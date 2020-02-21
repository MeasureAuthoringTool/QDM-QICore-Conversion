package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
@Slf4j
public class LibraryFileNotFoundException extends RuntimeException {
    private static final String MESSAGE = "Cannot find file with name: %s";

    public LibraryFileNotFoundException(String name) {
        super(String.format(MESSAGE, name));
        log.warn(getMessage());
    }
}
