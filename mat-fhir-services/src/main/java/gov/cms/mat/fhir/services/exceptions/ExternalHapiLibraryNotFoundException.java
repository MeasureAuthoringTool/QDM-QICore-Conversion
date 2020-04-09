package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
@Slf4j
public class ExternalHapiLibraryNotFoundException extends RuntimeException {
    private static final String NOT_FOUND_BY_FIND_DATA = "Cannot find a Hapi Fhir external Library with name: %s, version: %s";

    public ExternalHapiLibraryNotFoundException(String name, String version) {
        super(String.format(NOT_FOUND_BY_FIND_DATA, name, version));
        log.warn(getMessage());
    }
}
