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


    ValueSetOrchestrationService(ValueSetService valueSetService,
                                 ValueSetFhirValidationResults valueSetFhirValidationResults,
                                 ValueSetVsacVerifier valueSetVsacVerifier) {
        this.valueSetService = valueSetService;
        this.valueSetFhirValidationResults = valueSetFhirValidationResults;

    }

    boolean validate(OrchestrationProperties properties) {
        log.info("Validating ValueSet results for measure: {}", properties.getMatMeasure().getId());

        List<ValueSet> valueSets =
                valueSetService.findValueSetsByMeasure(properties);

        valueSetFhirValidationResults.collectResults(valueSets, properties.getMatMeasure().getId());

        ConversionResult conversionResult = ConversionReporter.getConversionResult();

        boolean noMissingDataSets =
                processMissingValueSets(conversionResult.getValueSetConversionResults().getValueSetResults());

        boolean result =
                noMissingDataSets && resultPass(conversionResult.getValueSetConversionResults().getValueSetFhirValidationErrors());

        log.info("Validate results for measure:{} ValueSet results: {}", properties.getMatMeasure().getId(), result);

        return result;
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

            log.info("Missing ValueSets results: {}", noErrors.get());

            return noErrors.get();
        }
    }

    private boolean haveError(ValueSetResult valueSetResult, AtomicBoolean noErrors) {
        if (BooleanUtils.isTrue(valueSetResult.getSuccess())) {
            return false;
        } else {
            noErrors.set(false);
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
            boolean haveErrorsOrHigher = valueSetFhirValidationErrors.stream()
                    .filter(r -> !r.getFhirValidationResults().isEmpty())
                    .map(ValueSetValidationResult::getFhirValidationResults)
                    .flatMap(List::stream)
                    .anyMatch(v -> checkSeverity(v.getSeverity()));

            log.info("FhirValidationErrors ValueSets resultPass: {}", haveErrorsOrHigher);

            return !haveErrorsOrHigher; // if none are ERROR or higher then flip the bit, we have no errors and PASS :)
        }
    }

    private boolean checkSeverity(String severity) {
        try {
            ResultSeverityEnum resultSeverityEnum = ResultSeverityEnum.valueOf(severity);

            log.trace("resultSeverityEnum: {}", resultSeverityEnum);

            return resultSeverityEnum.equals(ResultSeverityEnum.ERROR) ||
                    resultSeverityEnum.equals(ResultSeverityEnum.FATAL);
        } catch (IllegalArgumentException e) {
            log.error("Cannot find ResultSeverityEnum type from string: {}", severity);
            return false;
        }
    }
}
