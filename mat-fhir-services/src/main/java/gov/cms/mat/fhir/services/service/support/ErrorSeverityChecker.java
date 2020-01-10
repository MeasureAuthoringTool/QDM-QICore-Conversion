package gov.cms.mat.fhir.services.service.support;

import ca.uhn.fhir.validation.ResultSeverityEnum;

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
}
