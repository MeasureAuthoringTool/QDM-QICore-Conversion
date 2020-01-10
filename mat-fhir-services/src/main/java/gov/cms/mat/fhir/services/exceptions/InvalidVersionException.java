package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(value = HttpStatus.BAD_REQUEST)
@Slf4j
public class InvalidVersionException extends RuntimeException {
    public InvalidVersionException(String message) {
        super(message);
        log.warn(message);
    }
}