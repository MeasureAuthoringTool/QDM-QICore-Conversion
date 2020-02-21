package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HealthNullStatusException extends RuntimeException {
    private static final String MESSAGE = "Health results were null";

    public HealthNullStatusException() {
        super(MESSAGE);
        log.warn(MESSAGE);
    }
}
