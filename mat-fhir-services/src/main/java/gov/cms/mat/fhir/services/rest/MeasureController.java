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
import gov.cms.mat.fhir.services.components.mongo.ConversionResult;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.components.mongo.ConversionType;
import gov.cms.mat.fhir.services.components.xml.MatXmlProcessor;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.service.MeasureService;
import gov.cms.mat.fhir.services.summary.FhirMeasureResourceValidationResult;
import gov.cms.mat.fhir.services.translate.ManageMeasureDetailMapper;
import gov.cms.mat.fhir.services.translate.MeasureMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import mat.client.measure.ManageCompositeMeasureDetailModel;
import org.apache.commons.lang3.ArrayUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/measure")
@Tag(name = "Measure-Controller", description = "API for converting MAT Measures to FHIR")
@Slf4j
public class MeasureController implements FhirValidatorProcessor {
    private final MeasureService measureService;
    private final MeasureExportRepository measureExportRepo;
    private final ManageMeasureDetailMapper manageMeasureDetailMapper;
    private final HapiFhirServer hapiFhirServer;
    private final ConversionResultsService conversionResultsService;
    private final SupplementalDataProcessor supplementalDataProcessor;
    private final RiskAdjustmentsDataProcessor riskAdjustmentsDataProcessor;
    private final MeasureGroupingDataProcessor measureGroupingDataProcessor;

    private final MatXmlProcessor matXmlProcessor;

    public MeasureController(MeasureService measureService,
                             MeasureExportRepository measureExportRepository,
                             ManageMeasureDetailMapper manageMeasureDetailMapper,
                             HapiFhirServer hapiFhirServer,
                             ConversionResultsService conversionResultsService,
                             SupplementalDataProcessor supplementalDataProcessor,
                             RiskAdjustmentsDataProcessor riskAdjustmentsDataProcessor,
                             MeasureGroupingDataProcessor measureGroupingDataProcessor,
                             MatXmlProcessor matXmlProcessor) {
        this.measureService = measureService;

        this.measureExportRepo = measureExportRepository;
        this.manageMeasureDetailMapper = manageMeasureDetailMapper;
        this.hapiFhirServer = hapiFhirServer;
        this.conversionResultsService = conversionResultsService;
        this.supplementalDataProcessor = supplementalDataProcessor;
        this.riskAdjustmentsDataProcessor = riskAdjustmentsDataProcessor;
        this.measureGroupingDataProcessor = measureGroupingDataProcessor;
        this.matXmlProcessor = matXmlProcessor;
    }


    @Operation(summary = "Translate Measure in MAT to FHIR.",
            description = "Translate one Measure in the MAT Database and persist to the HAPI FHIR Database.")
    @PutMapping(path = "/translateMeasure")
    public TranslationOutcome translateMeasureById(
            @RequestParam("id") String id,
            @RequestParam(required = false, defaultValue = "SIMPLE") XmlSource xmlSource) {

        TranslationOutcome res = new TranslationOutcome();
        ConversionReporter.setInThreadLocal(id, conversionResultsService);
        ConversionReporter.resetMeasure(ConversionType.CONVERSION);

        try {
            Measure qdmMeasure = measureService.findOneValid(id);
            res.setFhirIdentity("Measure/" + qdmMeasure.getId());

            MeasureExport measureExport = measureExportRepo.getMeasureExportById(id);

            byte[] xmlBytes = findXml(qdmMeasure, xmlSource);


            String narrative = getNarrative(measureExport);

            org.hl7.fhir.r4.model.Measure fhirMeasure = getMeasure(qdmMeasure, xmlBytes, narrative);

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

    @Operation(summary = "Translate all Measures in MAT by status to FHIR.",
            description = "Translate all the Measures in the MAT Database by status and persist to the HAPI FHIR Database.")
    @PutMapping(path = "/translateMeasuresByStatus")
    public List<TranslationOutcome> translateMeasuresByStatus(
            @RequestParam("measureStatus") String measureStatus,
            @RequestParam(required = false, defaultValue = "SIMPLE") XmlSource xmlSource) {
        List<TranslationOutcome> res = new ArrayList<>();

        try {
            List<Measure> measureList = measureService.getMeasuresByStatus(measureStatus);

            for (Measure measure : measureList) {
                String measureId = measure.getId().trim();
                log.debug("Translating Measure measureId: {}", measureId);
                TranslationOutcome result = translateMeasureById(measureId, xmlSource);
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

    @Operation(summary = "Translate all Measures in MAT to FHIR.",
            description = "Translate all the Measures in the MAT Database and persist to the HAPI FHIR Database.")
    @PutMapping(path = "/translateAllMeasures")
    public List<TranslationOutcome> translateAllMeasures(
            @RequestParam(required = false, defaultValue = "SIMPLE") XmlSource xmlSource) {

        List<TranslationOutcome> res = new ArrayList<>();

        try {
            List<Measure> measureList = measureService.findAllValid();

            for (Measure measure : measureList) {
                String measureId = measure.getId().trim();

                log.debug("Translating Measure: {} ", measureId);
                TranslationOutcome result = translateMeasureById(measureId, xmlSource);
                res.add(result);
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

    @Operation(summary = "Delete all persisted FHIR Measures.",
            description = "Delete all the Measures in the HAPI FHIR Database.")
    @DeleteMapping(path = "/removeAllMeasures")
    public TranslationOutcome removeAllMeasures() {
        TranslationOutcome res = new TranslationOutcome();
        try {
            List<Measure> measureList = measureService.findAllValid();

            for (Measure measure : measureList) {
                String measureId = measure.getId().trim();
                deleteMeasure(measureId);
            }
        } catch (Exception ex) {
            res.setSuccessful(Boolean.FALSE);
            res.setMessage("/measure/removeAllMeasures Failed " + ex.getMessage());
            log.error("Failed Batch Delete of Measures ALL:", ex);
        }

        return res;
    }


    @Operation(summary = "Validate Measure",
            description = "Validate a Measure for conformance prior to persisting it to FHIR Resource Server")
    @PostMapping(path = "/validateMeasure")
    public FhirMeasureResourceValidationResult validateMeasure(
            @RequestParam("id") String id,
            @RequestParam(required = false, defaultValue = "SIMPLE") XmlSource xmlSource) {
        FhirMeasureResourceValidationResult response = new FhirMeasureResourceValidationResult(id, "Measure");

        ConversionReporter.setInThreadLocal(id, conversionResultsService);
        ConversionReporter.resetMeasure(ConversionType.VALIDATION);

        try {
            Measure qdmMeasure = measureService.findOneValid(id);

            MeasureExport measureExport = measureExportRepo.getMeasureExportById(id);

            byte[] xmlBytes = findXml(qdmMeasure, xmlSource);

            //human-readable may exist not an error if it doesn't
            String narrative = getNarrative(measureExport);

            org.hl7.fhir.r4.model.Measure fhirMeasure = getMeasure(qdmMeasure, xmlBytes, narrative);
            validateResource(response, fhirMeasure, hapiFhirServer.getCtx());

            processConversionResult(response);

            log.debug("Validated measureId: {}", id);
        } catch (Exception ex) {
            log.debug("Validation of Fhir Measure Failed for measureId: {}", id, ex);
        }

        return response;
    }
    
    @Operation(summary = "FHIR Measure",
            description = "Get FHIR Measure by Measure Id")
    @GetMapping(path = "/getFHIRMeasure")
    public String getFHIRMeasure(
            @RequestParam("id") String id) {
        String res = "";
        try {
           Bundle bundle = hapiFhirServer.getMeasure(id);
           res = hapiFhirServer.getCtx().newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);

        } catch (Exception ex) {
            log.debug("Get FHIR Measure Failed: {}", id, ex);
        }
        return res;
    }
    

    public void deleteMeasure(String measureId) {
        try {
            IGenericClient client = hapiFhirServer.getHapiClient();
            client.delete().resourceById(new IdDt("Measure", measureId)).execute();
        } catch (Exception ex) {
            log.trace("Error deleting measure with measureId: {}", measureId, ex);
        }
    }

    public void processConversionResult(FhirMeasureResourceValidationResult response) {
        List<ConversionResult.FhirValidationResult> list = buildResults(response);
        ConversionReporter.setFhirMeasureValidationResults(list);

        ConversionResult conversionResult = ConversionReporter.getConversionResult();
        response.setMeasureResults(conversionResult.getMeasureResults());
        response.setMeasureConversionType(conversionResult.getMeasureConversionType());
        response.setMeasureId(conversionResult.getMeasureId());

    }


    public org.hl7.fhir.r4.model.Measure getMeasure(Measure qdmMeasure, byte[] xmlBytes, String narrative) {
        ManageCompositeMeasureDetailModel model = manageMeasureDetailMapper.convert(xmlBytes, qdmMeasure);

        MeasureMapper fhirMapper = new MeasureMapper(model, narrative, hapiFhirServer.getBaseURL());
        org.hl7.fhir.r4.model.Measure fhirMeasure = fhirMapper.translateToFhir();

        if (ArrayUtils.isNotEmpty(xmlBytes)) {
            String xml = new String(xmlBytes);

            fhirMeasure.setSupplementalData(supplementalDataProcessor.processXml(xml));
            fhirMeasure.setRiskAdjustment(riskAdjustmentsDataProcessor.processXml(xml));

            fhirMeasure.setGroup(measureGroupingDataProcessor.processXml(xml));
        }
        return fhirMeasure;
    }

    public String getNarrative(MeasureExport measureExport) {
        try {
            return new String(measureExport.getHumanReadable());
        } catch (Exception ex) {
            log.error("Narrative not found: {}", ex.getMessage());
            return "";
        }
    }

    private byte[] findXml(Measure qdmMeasure, XmlSource xmlSource) {
        return matXmlProcessor.getXml(qdmMeasure, xmlSource);
    }
}
