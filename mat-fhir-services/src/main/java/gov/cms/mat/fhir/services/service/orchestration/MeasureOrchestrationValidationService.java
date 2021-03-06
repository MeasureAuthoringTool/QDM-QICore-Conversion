package gov.cms.mat.fhir.services.service.orchestration;


import gov.cms.mat.fhir.rest.dto.FhirValidationResult;
import gov.cms.mat.fhir.services.components.reporting.ConversionReporter;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.service.support.ErrorSeverityChecker;
import gov.cms.mat.fhir.services.summary.FhirMeasureResourceValidationResult;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import gov.cms.mat.fhir.services.translate.MeasureTranslator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.MEASURE_VALIDATION_FAILED;
import static gov.cms.mat.fhir.services.components.reporting.HapiResourcePersistedState.NEW;

@Component
@Slf4j
public class MeasureOrchestrationValidationService implements ErrorSeverityChecker {
    private static final String FAILURE_MESSAGE = "Measure validation failed";

    private final FhirValidatorProcessor fhirValidatorProcessor;
    private final HapiFhirServer hapiFhirServer;
    private final MeasureTranslator measureTranslator;

    public MeasureOrchestrationValidationService(FhirValidatorProcessor fhirValidatorProcessor, HapiFhirServer hapiFhirServer,
                                                 MeasureTranslator measureTranslator) {
        this.fhirValidatorProcessor = fhirValidatorProcessor;
        this.hapiFhirServer = hapiFhirServer;
        this.measureTranslator = measureTranslator;
    }

    public boolean validate(OrchestrationProperties properties) {
        log.info("Validating measure hapi measureId: {}", properties.getMeasureId());

        FhirMeasureResourceValidationResult response =
                new FhirMeasureResourceValidationResult(properties.getMeasureId(), "Measure");

        if (properties.isPush()) {
            org.hl7.fhir.r4.model.Measure fhirMeasure = processFhirMeasure(properties);
            fhirValidatorProcessor.validateResource(response, fhirMeasure);
        }

        List<FhirValidationResult> list = fhirValidatorProcessor.buildResults(response);

        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(validationResult -> log.debug("FhirValidationResult error: {}", validationResult));
        }

        ConversionReporter.setFhirMeasureValidationResults(list);

        AtomicBoolean atomicBoolean = new AtomicBoolean(Boolean.TRUE);
        list.forEach(v -> isValid(v, atomicBoolean));

        if (!atomicBoolean.get()) {
            ConversionReporter.setTerminalMessage(FAILURE_MESSAGE, MEASURE_VALIDATION_FAILED);
        }

        return atomicBoolean.get();
    }

    public List<FhirValidationResult> verify(OrchestrationProperties properties) {
        log.info("Validating measure hapi measureId: {}", properties.getMeasureId());

        FhirMeasureResourceValidationResult response =
                new FhirMeasureResourceValidationResult(properties.getMeasureId(), "Measure");

        if (properties.isPush()) {
            org.hl7.fhir.r4.model.Measure fhirMeasure = processFhirMeasure(properties);
            fhirValidatorProcessor.validateResource(response, fhirMeasure);
        }

        List<FhirValidationResult> list = fhirValidatorProcessor.buildResults(response);

        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(validationResult -> log.debug("FhirValidationResult error: {}", validationResult));
        }

        ConversionReporter.setFhirMeasureValidationResults(list);

        AtomicBoolean atomicBoolean = new AtomicBoolean(Boolean.TRUE);
        StringBuilder validationError = new StringBuilder();
        list.forEach(v -> {
            isValid(v, atomicBoolean);
            validationError.append(v + "\n");
        });

        if (!atomicBoolean.get()) {
            ConversionReporter.setTerminalMessage(FAILURE_MESSAGE, MEASURE_VALIDATION_FAILED);
            return list;
        }
        return null;
    }

    private org.hl7.fhir.r4.model.Measure processFhirMeasure(OrchestrationProperties properties) {
        org.hl7.fhir.r4.model.Measure fhirMeasure = measureTranslator.translateToFhir(properties.getMeasureId());

        properties.setFhirMeasure(fhirMeasure);
        ConversionReporter.setMeasureValidationLink(null, NEW);

        String json = hapiFhirServer.toJson(fhirMeasure);
        log.debug("Measure json: {}", json);
        ConversionReporter.setFhirMeasureJson(json);

        return fhirMeasure;
    }
}
