package gov.cms.mat.fhir.services.service.orchestration;


import gov.cms.mat.fhir.rest.dto.FhirValidationResult;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.service.support.ErrorSeverityChecker;
import gov.cms.mat.fhir.services.summary.FhirMeasureResourceValidationResult;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import gov.cms.mat.fhir.services.translate.MeasureTranslator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.MEASURE_VALIDATION_FAILED;
import static gov.cms.mat.fhir.services.components.mongo.HapiResourcePersistedState.NEW;

@Component
@Slf4j
public class MeasureOrchestrationValidationService implements FhirValidatorProcessor, ErrorSeverityChecker {
    private static final String FAILURE_MESSAGE = "Measure validation failed";

    private final HapiFhirServer hapiFhirServer;
    private final MeasureTranslator measureTranslator;

    public MeasureOrchestrationValidationService(HapiFhirServer hapiFhirServer,
                                                 MeasureTranslator measureTranslator) {
        this.hapiFhirServer = hapiFhirServer;
        this.measureTranslator = measureTranslator;
    }

    public boolean validate(OrchestrationProperties properties) {
        log.info("Validating measure hapi measureId: {}", properties.getMeasureId());
        return validateMeasure(properties);
    }

    private boolean validateMeasure(OrchestrationProperties properties) {
        FhirMeasureResourceValidationResult response =
                new FhirMeasureResourceValidationResult(properties.getMeasureId(), "Measure");

        if (properties.getIsPush()) {
            org.hl7.fhir.r4.model.Measure fhirMeasure = processFhirMeasure(properties);
            validateResource(response, fhirMeasure, hapiFhirServer.getCtx());
        }

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
        return measureTranslator.translateToFhir(properties.getMeasureId());
    }
}
