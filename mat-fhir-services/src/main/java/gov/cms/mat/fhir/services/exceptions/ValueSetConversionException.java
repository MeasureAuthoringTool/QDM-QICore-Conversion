package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
@Slf4j
public class ValueSetConversionException extends RuntimeException {
    public ValueSetConversionException(String message, Exception e) {
        super(message, e);
    }

    public ValueSetConversionException(String message) {
        this(message, null);
    }
}
