package gov.cms.mat.fhir.services.service.support;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import gov.cms.mat.fhir.rest.dto.FhirValidationResult;

import java.util.concurrent.atomic.AtomicBoolean;

public interface ErrorSeverityChecker {
    default boolean checkSeverity(String severity) {
        try {
            ResultSeverityEnum resultSeverityEnum = ResultSeverityEnum.valueOf(severity.toUpperCase());

            return resultSeverityEnum.equals(ResultSeverityEnum.ERROR) ||
                    resultSeverityEnum.equals(ResultSeverityEnum.FATAL);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    default void isValid(FhirValidationResult v, AtomicBoolean atomicBoolean) {
        boolean haveErrorsOrHigher = checkSeverity(v.getSeverity());

        if (haveErrorsOrHigher) {
            atomicBoolean.set(false);
        }
    }
}
