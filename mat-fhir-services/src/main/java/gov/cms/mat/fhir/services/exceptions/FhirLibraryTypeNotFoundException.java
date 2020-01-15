package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
@Slf4j
public class FhirLibraryTypeNotFoundException extends RuntimeException {
    private static final String NOT_FOUND_BY_ID = "Cannot find a FhirLibrary type: %s for id: %s";

    public FhirLibraryTypeNotFoundException(String type, String id) {
        super(String.format(NOT_FOUND_BY_ID, id, type));
        log.warn(getMessage());
    }
}
