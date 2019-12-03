/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.services.rest;

import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.commons.objects.TranslationOutcome;
import gov.cms.mat.fhir.services.components.fhir.MeasureGroupingDataProcessor;
import gov.cms.mat.fhir.services.components.fhir.RiskAdjustmentsDataProcessor;
import gov.cms.mat.fhir.services.components.fhir.SupplementalDataProcessor;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import gov.cms.mat.fhir.services.repository.MeasureRepository;
import gov.cms.mat.fhir.services.translate.ManageMeasureDetailMapper;
import gov.cms.mat.fhir.services.translate.MeasureMapper;
import lombok.extern.slf4j.Slf4j;
import mat.client.measure.ManageCompositeMeasureDetailModel;
import org.apache.commons.lang3.ArrayUtils;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping(path = "/measure")
@Slf4j
public class MeasureController {
    private final MeasureRepository measureRepo;
    private final MeasureExportRepository measureExportRepo;
    private final ManageMeasureDetailMapper manageMeasureDetailMapper;
    private final HapiFhirServer hapiFhirServer;
    private final ConversionResultsService conversionResultsService;
    private final SupplementalDataProcessor supplementalDataProcessor;
    private final RiskAdjustmentsDataProcessor riskAdjustmentsDataProcessor;
    private final MeasureGroupingDataProcessor measureGroupingDataProcessor;

    public MeasureController(MeasureRepository measureRepository,
                                     MeasureExportRepository measureExportRepository,
                                     ManageMeasureDetailMapper manageMeasureDetailMapper,
                                     HapiFhirServer hapiFhirServer,
                                     ConversionResultsService conversionResultsService,
                                     SupplementalDataProcessor supplementalDataProcessor,
                                     RiskAdjustmentsDataProcessor riskAdjustmentsDataProcessor,
                                     MeasureGroupingDataProcessor measureGroupingDataProcessor) {
        this.measureRepo = measureRepository;

        this.measureExportRepo = measureExportRepository;
        this.manageMeasureDetailMapper = manageMeasureDetailMapper;
        this.hapiFhirServer = hapiFhirServer;
        this.conversionResultsService = conversionResultsService;
        this.supplementalDataProcessor = supplementalDataProcessor;
        this.riskAdjustmentsDataProcessor = riskAdjustmentsDataProcessor;
        this.measureGroupingDataProcessor = measureGroupingDataProcessor;
    }


    @PutMapping(path = "/translateMeasure")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public TranslationOutcome translateMeasureById(@QueryParam("id") String id) {
        TranslationOutcome res = new TranslationOutcome();
        ConversionReporter.setInThreadLocal(id, conversionResultsService);
        ConversionReporter.resetMeasure();

        try {
            Measure qdmMeasure = measureRepo.getMeasureById(id);
            res.setFhirIdentity("Measure/" + qdmMeasure.getId());
            MeasureExport measureExport = measureExportRepo.getMeasureExportById(id);
            byte[] xmlBytes = measureExport.getSimpleXml();
            //humanreadible may exist not an error if it doesn't
            String narrative = "";
            try {
                narrative = new String(measureExport.getHumanReadable());
            } catch (Exception ex) {
                log.error("Narrative not found: {}", ex.getMessage());
            }

            ManageCompositeMeasureDetailModel model = manageMeasureDetailMapper.convert(xmlBytes, qdmMeasure);

            MeasureMapper fhirMapper = new MeasureMapper(model, narrative, hapiFhirServer.getBaseURL());
            org.hl7.fhir.r4.model.Measure fhirMeasure = fhirMapper.translateToFhir();

            if (ArrayUtils.isNotEmpty(xmlBytes)) {
                String xml = new String(xmlBytes);

                fhirMeasure.setSupplementalData(supplementalDataProcessor.processXml(xml));
                fhirMeasure.setRiskAdjustment(riskAdjustmentsDataProcessor.processXml(xml));

                fhirMeasure.setGroup(measureGroupingDataProcessor.processXml(xml));
                log.debug("Processed");
            }

            Bundle bundle = hapiFhirServer.createAndExecuteBundle(fhirMeasure);
            log.debug("bundle: {}", bundle);

        } catch (Exception ex) {
            res.setSuccessful(Boolean.FALSE);
            if (ex.getMessage() == null) {
                res.setMessage("/measure/translateMeasure Failed " + id + " No SimpleXML Available");
            } else {
                res.setMessage("/measure/translateMeasure Failed " + id + " " + ex.getMessage());
            }
            log.error("Failed to Translate Measure", ex);
        }
        return res;
    }


    @PutMapping(path = "/translateMeasuresByStatus")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<TranslationOutcome> translateMeasuresByStatus(@QueryParam("measureStatus") String measureStatus) {
        List<TranslationOutcome> res = new ArrayList<>();
        try {
            List<Measure> measureList = measureRepo.getMeasuresByStatus(measureStatus);
            Iterator iter = measureList.iterator();
            while (iter.hasNext()) {
                Measure measure = (Measure) iter.next();
                String measureId = measure.getId().trim();
                log.debug("Translating Measure measureId: {}", measureId);
                TranslationOutcome result = translateMeasureById(measureId);
                res.add(result);
            }
        } catch (Exception ex) {
            TranslationOutcome tOut = new TranslationOutcome();
            tOut.setSuccessful(Boolean.FALSE);
            tOut.setMessage("/measure/translateMeasuresByStatus Failed " + ex.getMessage());
            res.add(tOut);
            log.error("Failed Batch Translation of Measures: ", ex);
        }
        ConversionReporter.removeInThreadLocal();
        return res;
    }

    @PutMapping(path = "/translateAllMeasures")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<TranslationOutcome> translateAllMeasures() {
        List<TranslationOutcome> res = new ArrayList<>();
        try {
            List<Measure> measureList = measureRepo.findAll();
            Iterator iter = measureList.iterator();
            while (iter.hasNext()) {
                Measure measure = (Measure) iter.next();
                String measureId = measure.getId().trim();
                String version = measure.getReleaseVersion();

                if (version != null) {
                    if (version.equals("v5.5") || version.equals("v5.6") || version.equals("v5.7") || version.equals("v5.8")) {
                        log.debug("Translating Measure: {} ", measureId);
                        TranslationOutcome result = translateMeasureById(measureId);
                        res.add(result);
                    }
                }
            }
        } catch (Exception ex) {
            TranslationOutcome tOut = new TranslationOutcome();
            tOut.setSuccessful(Boolean.FALSE);
            tOut.setMessage("/measure/translateAllMeasures Failed " + ex.getMessage());
            res.add(tOut);
            log.error("Failed Batch Translation of Measures ALL:", ex);
        }
        ConversionReporter.removeInThreadLocal();
        return res;
    }

    @DeleteMapping(path = "/removeAllMeasures")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public TranslationOutcome removeAllMeasures() {
        TranslationOutcome res = new TranslationOutcome();
        try {
            List<Measure> measureList = measureRepo.findAll();
            Iterator iter = measureList.iterator();
            while (iter.hasNext()) {
                Measure measure = (Measure) iter.next();
                String measureId = measure.getId().trim();
                try {
                    IGenericClient client = hapiFhirServer.getHapiClient();
                    IBaseOperationOutcome resp = client.delete().resourceById(new IdDt("Measure", measureId)).execute();
                } catch (Exception ex) {
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            res.setSuccessful(Boolean.FALSE);
            res.setMessage("/measure/removeAllMeasures Failed " + ex.getMessage());
            log.error("Failed Batch Delete of Measures ALL: {}", ex.getMessage());
        }

        return res;
    }
}