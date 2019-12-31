package gov.cms.mat.fhir.services.service.orchestration;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import gov.cms.mat.fhir.rest.dto.FhirValidationResult;
import gov.cms.mat.fhir.rest.dto.ValueSetResult;
import gov.cms.mat.fhir.rest.dto.ValueSetValidationResult;
import gov.cms.mat.fhir.services.components.fhir.ValueSetFhirValidationResults;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResult;
import gov.cms.mat.fhir.services.service.ValueSetService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import gov.cms.mat.fhir.services.translate.ValueSetVsacVerifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
class ValueSetOrchestrationService {
    private final ValueSetService valueSetService;
    private final ValueSetFhirValidationResults valueSetFhirValidationResults;
    private final ValueSetVsacVerifier valueSetVsacVerifier;

    ValueSetOrchestrationService(ValueSetService valueSetService,
                                 ValueSetFhirValidationResults valueSetFhirValidationResults,
                                 ValueSetVsacVerifier valueSetVsacVerifier) {
        this.valueSetService = valueSetService;
        this.valueSetFhirValidationResults = valueSetFhirValidationResults;
        this.valueSetVsacVerifier = valueSetVsacVerifier;
    }

    boolean process(OrchestrationProperties properties) {
        List<ValueSet> valueSets =
                valueSetService.findValueSetsByMeasure(properties);

        valueSetFhirValidationResults.collectResults(valueSets, properties.getMatMeasure().getId());

        ConversionResult conversionResult = ConversionReporter.getConversionResult();

        boolean missingDataSets =
                processMissingValueSets(conversionResult.getValueSetConversionResults().getValueSetResults());

        return !missingDataSets && resultPass(conversionResult.getValueSetConversionResults().getValueSetFhirValidationErrors());
    }

    private boolean processMissingValueSets(List<ValueSetResult> valueSetResults) {
        if (CollectionUtils.isEmpty(valueSetResults)) {
            log.warn("No value set results");
            return false;
        } else {
            AtomicBoolean noErrors = new AtomicBoolean(true);

            valueSetResults.stream()
                    .filter(v -> haveError(v, noErrors))
                    .forEach(this::processMissingValueSet);

            return noErrors.get();
        }
    }

    private boolean haveError(ValueSetResult valueSetResult, AtomicBoolean noErrors) {
        if (BooleanUtils.isTrue(valueSetResult.getSuccess())) {
            return false;
        } else {
            noErrors.set(true);
            return true;
        }
    }

    private void processMissingValueSet(ValueSetResult valueSetResult) {
        FhirValidationResult fhirValidationResult = FhirValidationResult.builder()
                .severity(ResultSeverityEnum.FATAL.name())
                .errorDescription(valueSetResult.getReason())
                .locationField("ValueSet")
                .build();

        ConversionReporter.setValueSetsValidationResult(valueSetResult.getOid(), fhirValidationResult);
    }

    private boolean resultPass(List<ValueSetValidationResult> valueSetFhirValidationErrors) {
        if (CollectionUtils.isEmpty(valueSetFhirValidationErrors)) {
            return true;
        } else {
            return valueSetFhirValidationErrors.stream()
                    .filter(r -> !r.getFhirValidationResults().isEmpty())
                    .map(ValueSetValidationResult::getFhirValidationResults)
                    .flatMap(List::stream)
                    .anyMatch(v -> checkSeverity(v.getSeverity()));
        }
    }

    private boolean checkSeverity(String severity) {
        try {
            ResultSeverityEnum resultSeverityEnum = ResultSeverityEnum.valueOf(severity);

            return resultSeverityEnum.equals(ResultSeverityEnum.ERROR) ||
                    resultSeverityEnum.equals(ResultSeverityEnum.FATAL);
        } catch (IllegalArgumentException e) {
            log.error("Cannot find ResultSeverityEnum type from string: {}", severity);
            return false;
        }
    }
}
