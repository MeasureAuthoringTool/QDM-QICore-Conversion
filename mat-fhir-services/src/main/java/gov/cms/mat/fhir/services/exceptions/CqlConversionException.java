package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
@Slf4j
public class CqlConversionException extends RuntimeException {

    public CqlConversionException(String message) {
        this(message, null);
    }

    public CqlConversionException(String message, Exception cause) {
        super(message, cause);
        log.warn(getMessage());
    }
}
