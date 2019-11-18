/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.services.rest;

import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.CqlLibraryExport;
import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.commons.objects.TranslationOutcome;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.repository.CqlLibraryExportRepository;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import gov.cms.mat.fhir.services.repository.MeasureRepository;
import gov.cms.mat.fhir.services.translate.LibraryMapper;
import gov.cms.mat.fhir.services.translate.ManageMeasureDetailMapper;
import gov.cms.mat.fhir.services.translate.MeasureMapper;
import lombok.extern.slf4j.Slf4j;
import mat.client.measure.ManageCompositeMeasureDetailModel;
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
@RequestMapping(path = "/qdmtofhir")
@Slf4j
public class MeasureTranslationService {
    private final MeasureRepository measureRepo;
    private final MeasureExportRepository measureExportRepo;
    private final ManageMeasureDetailMapper manageMeasureDetailMapper;
    private final HapiFhirServer hapiFhirServer;
    private final CqlLibraryRepository cqlLibraryRepo;
    private final CqlLibraryExportRepository cqlLibraryExportRepo;
    private final ConversionResultsService conversionResultsService;

    public MeasureTranslationService(MeasureRepository measureRepository,
                                     MeasureExportRepository measureExportRepository,
                                     ManageMeasureDetailMapper manageMeasureDetailMapper,
                                     HapiFhirServer hapiFhirServer,
                                     CqlLibraryRepository cqlLibraryRepo,
                                     CqlLibraryExportRepository cqlLibraryExportRepo,
                                     ConversionResultsService conversionResultsService) {
        this.measureRepo = measureRepository;

        this.measureExportRepo = measureExportRepository;
        this.manageMeasureDetailMapper = manageMeasureDetailMapper;
        this.hapiFhirServer = hapiFhirServer;
        this.cqlLibraryExportRepo = cqlLibraryExportRepo;
        this.cqlLibraryRepo = cqlLibraryRepo;
        this.conversionResultsService = conversionResultsService;
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

            Bundle bundle = hapiFhirServer.createAndExecuteBundle(fhirMeasure);
            log.debug("bundle: {}", bundle);

        } catch (Exception ex) {
            res.setSuccessful(Boolean.FALSE);
            if (ex.getMessage() == null) {
                res.setMessage("/qdmtofhir/translateMeasure Failed " + id + " No SimpleXML Available");
            } else {
                res.setMessage("/qdmtofhir/translateMeasure Failed " + id + " " + ex.getMessage());
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
            tOut.setMessage("/qdmtofhir/translateMeasuresByStatus Failed " + ex.getMessage());
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
            tOut.setMessage("/qdmtofhir/translateAllMeasures Failed " + ex.getMessage());
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
            res.setMessage("/qdmtofhir removeAllMeasures Failed " + ex.getMessage());
            log.error("Failed Batch Delete of Measures ALL: {}", ex.getMessage());
        }

        return res;
    }

    @GetMapping(path = "/translateLibraryByMeasureId")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public TranslationOutcome translateLibraryByMeasureId(@QueryParam("id") String id) {
        TranslationOutcome res = new TranslationOutcome();
        ConversionReporter.setInThreadLocal(id, conversionResultsService);
        ConversionReporter.resetLibrary();

        try {
            List<CqlLibrary> cqlLibs = cqlLibraryRepo.getCqlLibraryByMeasureId(id);
            Iterator iter = cqlLibs.iterator();
            while (iter.hasNext()) {
                CqlLibrary cqlLib = (CqlLibrary) iter.next();
                //go get the associated cql and elm
                CqlLibraryExport cqlExp = cqlLibraryExportRepo.getCqlLibraryExportByCqlLibraryId(cqlLib.getId());
                byte[] cql = cqlExp.getCql();
                byte[] elm = cqlExp.getElm();
                LibraryMapper fhirMapper = new LibraryMapper(cqlLib, cql, elm, hapiFhirServer.getBaseURL());
                org.hl7.fhir.r4.model.Library fhirLibrary = fhirMapper.translateToFhir();

                Bundle bundle = hapiFhirServer.createAndExecuteBundle(fhirLibrary);

                IGenericClient client = hapiFhirServer.getHapiClient();
                Bundle resp = client.transaction().withBundle(bundle).execute();

                // Log the response
                log.info(hapiFhirServer.getCtx().newXmlParser().setPrettyPrint(true).encodeResourceToString(resp));
            }
        } catch (Exception ex) {
            res.setSuccessful(Boolean.FALSE);
            if (ex.getMessage() == null) {
                res.setMessage("/qdmtofhir/translateLibrary Failed " + id + "Missing");
            } else {
                res.setMessage("/qdmtofhir/translateLibrary Failed " + id + " " + ex.getMessage());
            }
            log.error("Failed to Translate Library: {}", ex.getMessage());
        }
        return res;
    }


    @GetMapping(path = "/translateAllLibraries")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<TranslationOutcome> translateAllLibraries() {
        List<TranslationOutcome> res = new ArrayList();
        try {
            List<Measure> measureList = measureRepo.findAll();
            Iterator iter = measureList.iterator();
            while (iter.hasNext()) {
                Measure measure = (Measure) iter.next();
                String measureId = measure.getId().trim();
                String version = measure.getReleaseVersion();

                if (version != null) {
                    if (version.equals("v5.5") || version.equals("v5.6") || version.equals("v5.7") || version.equals("v5.8")) {
                        System.out.println("Translating Libraries for " + measureId);
                        TranslationOutcome result = translateLibraryByMeasureId(measureId);
                        res.add(result);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            TranslationOutcome tOut = new TranslationOutcome();
            tOut.setSuccessful(Boolean.FALSE);
            tOut.setMessage("/qdmtofhir/translateAllLibraries Failed " + ex.getMessage());
            res.add(tOut);
            log.error("Failed Batch Translation of Libraries ALL: {}", ex.getMessage());
        }
        ConversionReporter.removeInThreadLocal();
        return res;
    }

    @DeleteMapping(path = "/removeAllLibraries")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public TranslationOutcome removeAllLibraries() {
        TranslationOutcome res = new TranslationOutcome();
        try {
            List<CqlLibraryExport> exportList = cqlLibraryExportRepo.findAll();
            Iterator iter = exportList.iterator();
            while (iter.hasNext()) {
                CqlLibraryExport library = (CqlLibraryExport) iter.next();
                String cqlId = library.getCqlLibraryId();
                try {
                    IGenericClient client = hapiFhirServer.getHapiClient();
                    IBaseOperationOutcome resp = client.delete().resourceById(new IdDt("Library", cqlId)).execute();
                } catch (Exception ex) {
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            res.setSuccessful(Boolean.FALSE);
            res.setMessage("/qdmtofhir removeAllLibraries Failed " + ex.getMessage());
            log.error("Failed Batch Delete of Libraries ALL: {}", ex.getMessage());
        }

        return res;
    }
}
