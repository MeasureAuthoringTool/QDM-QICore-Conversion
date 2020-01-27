package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.PRECONDITION_FAILED)
@Slf4j
public class SelfHealthCheckException extends RuntimeException {
    private static final String MESSAGE = "Self Health check failed with status: %s";

    public SelfHealthCheckException(Status status) {
        super(String.format(MESSAGE, status));
        log.info(getMessage());
    }
}
