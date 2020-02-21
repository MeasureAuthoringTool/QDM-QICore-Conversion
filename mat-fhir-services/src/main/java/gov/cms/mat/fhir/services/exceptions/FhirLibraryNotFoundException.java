package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
@Slf4j
public class FhirLibraryNotFoundException extends RuntimeException {
    private static final String NOT_FOUND_BY_ID = "Cannot find a FhirLibrary for Id: %s";

    public FhirLibraryNotFoundException(String measureId) {
        super(String.format(NOT_FOUND_BY_ID, measureId));
        log.warn(getMessage());
    }
}
