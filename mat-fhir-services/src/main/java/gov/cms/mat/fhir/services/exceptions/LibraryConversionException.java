package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
@Slf4j
public class LibraryConversionException extends RuntimeException {
    public LibraryConversionException(String message, Exception e) {
        super(message, e);
    }

    public LibraryConversionException(String message) {
        this(message, null);
    }
}
