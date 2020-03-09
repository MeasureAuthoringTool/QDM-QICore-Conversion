package gov.cms.mat.fhir.services.service.orchestration;


import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.rest.dto.FhirValidationResult;
import gov.cms.mat.fhir.services.components.mat.MatXmlException;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.xml.MatXmlProcessor;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.service.MeasureExportDataService;
import gov.cms.mat.fhir.services.service.support.ErrorSeverityChecker;
import gov.cms.mat.fhir.services.summary.FhirMeasureResourceValidationResult;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.MEASURE_CONVERSION_FAILED;
import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.MEASURE_VALIDATION_FAILED;
import static gov.cms.mat.fhir.services.components.mongo.HapiResourcePersistedState.NEW;

@Component
@Slf4j
public class MeasureOrchestrationValidationService implements FhirValidatorProcessor, ErrorSeverityChecker {
    private static final String FAILURE_MESSAGE = "Measure validation failed";

    private final MeasureExportDataService measureExportDataService;
    private final MatXmlProcessor matXmlProcessor;
    private final HapiFhirServer hapiFhirServer;

    private final FhirMeasureCreator fhirMeasureCreator;

    public MeasureOrchestrationValidationService(MeasureExportDataService measureExportDataService,
                                                 MatXmlProcessor matXmlProcessor,
                                                 HapiFhirServer hapiFhirServer, FhirMeasureCreator fhirMeasureCreator) {
        this.measureExportDataService = measureExportDataService;
        this.matXmlProcessor = matXmlProcessor;
        this.hapiFhirServer = hapiFhirServer;

        this.fhirMeasureCreator = fhirMeasureCreator;
    }

    boolean validate(OrchestrationProperties properties) {
//        if (ConversionReporter.getConversionResult().measureExistsInHapi()) {
//            log.info("No Validation performed already in hapi measureId: {}", properties.getMeasureId());
//            return true;
//        } else {
            log.info("Validating measure hapi measureId: {}", properties.getMeasureId());
            return validateMeasure(properties);
//        }
    }

    private boolean validateMeasure(OrchestrationProperties properties) {
        org.hl7.fhir.r4.model.Measure fhirMeasure = processFhirMeasure(properties);

        FhirMeasureResourceValidationResult response =
                new FhirMeasureResourceValidationResult(properties.getMeasureId(), "Measure");

        validateResource(response, fhirMeasure, hapiFhirServer.getCtx());

        List<FhirValidationResult> list = buildResults(response);

        ConversionReporter.setFhirMeasureValidationResults(list);

        AtomicBoolean atomicBoolean = new AtomicBoolean(Boolean.TRUE);
        list.forEach(v -> isValid(v, atomicBoolean));

        if (!atomicBoolean.get()) {
            ConversionReporter.setTerminalMessage(FAILURE_MESSAGE, MEASURE_VALIDATION_FAILED);
        }

        return atomicBoolean.get();
    }

    private org.hl7.fhir.r4.model.Measure processFhirMeasure(OrchestrationProperties properties) {
        org.hl7.fhir.r4.model.Measure fhirMeasure = buildFhirMeasure(properties);

        properties.setFhirMeasure(fhirMeasure);
        ConversionReporter.setMeasureValidationLink(null, NEW);
        ConversionReporter.setFhirMeasureJson(hapiFhirServer.toJson(fhirMeasure));

        return fhirMeasure;
    }

    private org.hl7.fhir.r4.model.Measure buildFhirMeasure(OrchestrationProperties properties) {
        byte[] xmlBytes = matXmlProcessor.getXml(properties.getMatMeasure(), properties.getXmlSource());

        MeasureExport measureExport = measureExportDataService.findByIdRequired(properties.getMeasureId());

        //human-readable may exist not an error if it doesn't
        String narrative = getNarrative(measureExport);

        return createFhirMeasure(properties.getMatMeasure(), xmlBytes, narrative);
    }

    public String getNarrative(MeasureExport measureExport) {
        try {
            return new String(measureExport.getHumanReadable());
        } catch (Exception ex) {
            log.warn("Narrative not found: {}", ex.getMessage());
            return "";
        }
    }

    public org.hl7.fhir.r4.model.Measure createFhirMeasure(Measure matMeasure, byte[] xmlBytes, String narrative) {
      //  try {
            return fhirMeasureCreator.create(matMeasure, xmlBytes, narrative);
       // } catch (MatXmlException e) {
       //     ConversionReporter.setTerminalMessage(e.getMessage(), MEASURE_CONVERSION_FAILED);
       //     throw e;
      //  }
    }
}
