package gov.cms.mat.fhir.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.extern.slf4j.Slf4j;


@ResponseStatus(value = HttpStatus.BAD_REQUEST)
@Slf4j
public class CqlParseException extends RuntimeException {

    public CqlParseException(Throwable e) {
        super(e);
    }

    public CqlParseException(String message) {
        super(message);
    }

    public CqlParseException(String message, Throwable e) {
        super(message, e);
    }
}