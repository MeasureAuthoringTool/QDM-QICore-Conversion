package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.rest.dto.ValueSetConversionResults;
import gov.cms.mat.fhir.services.components.fhir.ValueSetFhirValidationResults;
import gov.cms.mat.fhir.services.components.reporting.ConversionReporter;
import gov.cms.mat.fhir.services.components.reporting.ConversionResult;
import gov.cms.mat.fhir.services.service.ValueSetService;
import gov.cms.mat.fhir.services.service.support.ErrorSeverityChecker;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.VALUESET_VALIDATION_FAILED;

@Component
@Slf4j
class ValueSetOrchestrationValidationService implements ErrorSeverityChecker {
    private static final String FAILURE_MESSAGE = "ValueSet validation failed";
    private final ValueSetService valueSetService;
    private final ValueSetFhirValidationResults valueSetFhirValidationResults;

    ValueSetOrchestrationValidationService(ValueSetService valueSetService,
                                           ValueSetFhirValidationResults valueSetFhirValidationResults) {
        this.valueSetService = valueSetService;
        this.valueSetFhirValidationResults = valueSetFhirValidationResults;
    }

    boolean validate(OrchestrationProperties properties) {
        log.info("Validating ValueSet results for measure: {}", properties.getMeasureId());

        valueSetFhirValidationResults.collectResults(properties.getValueSets(), properties.getMeasureId());

        ConversionResult conversionResult = ConversionReporter.getConversionResult(); //Must be set up prior

        boolean noMissingDataSets =
                processMissingValueSets(conversionResult.getValueSetConversionResults(), properties.getMeasureId());

        boolean result =
                noMissingDataSets && resultPass(conversionResult.getValueSetConversionResults());

        if (!result) {
            ConversionReporter.setTerminalMessage(FAILURE_MESSAGE, VALUESET_VALIDATION_FAILED);
        }

        log.info("ValueSet validation results for measure:{}, passed: {}", properties.getMeasureId(), result);

        return result;
    }

    public List<ValueSet> getValueSetsNotInHapi(OrchestrationProperties properties) {
        // will throw ValueSetConversionException if none found in DB, then filters if not in hapi
        return valueSetService.findValueSetsByMeasure(properties);
    }

    private boolean processMissingValueSets(List<ValueSetConversionResults> valueSetConversionResults, String measureId) {
        if (CollectionUtils.isEmpty(valueSetConversionResults)) {
            log.warn("No valueSetConversionResults results");
            return false;
        } else {
            AtomicInteger errorCount = new AtomicInteger();

            valueSetConversionResults.forEach(v -> haveError(v.getSuccess(), errorCount));

            log.info("Measure: {} valueSetConversionResults contains {} errors", measureId, errorCount.get());

            return errorCount.get() == 0;
        }
    }

    private void haveError(Boolean success, AtomicInteger errorCount) {
        if (BooleanUtils.isFalse(success)) {
            errorCount.incrementAndGet();
        }
    }

    private boolean resultPass(List<ValueSetConversionResults> valueSetConversionResults) {
        if (CollectionUtils.isEmpty(valueSetConversionResults)) {
            return true;
        } else {
            AtomicBoolean atomicBoolean = new AtomicBoolean(Boolean.TRUE);
            valueSetConversionResults.forEach(v -> isValid(v, atomicBoolean));

            log.info("FhirValidationErrors ValueSets resultPass: {}", atomicBoolean.get());

            return atomicBoolean.get();
        }
    }

    private void isValid(ValueSetConversionResults v, AtomicBoolean atomicBoolean) {
        boolean haveErrorsOrHigher = v.getValueSetFhirValidationResults().stream().anyMatch(vv2 -> checkSeverity(vv2.getSeverity()));

        if (haveErrorsOrHigher) {
            atomicBoolean.set(false);
        } else {
            if (BooleanUtils.isFalse(v.getSuccess())) {
                atomicBoolean.set(false);
            }
        }
    }
}
