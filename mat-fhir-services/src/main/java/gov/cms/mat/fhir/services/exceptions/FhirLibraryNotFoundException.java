package gov.cms.mat.fhir.services.exceptions;

import gov.cms.mat.cql.elements.IncludeProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
@Slf4j
public class FhirLibraryNotFoundException extends RuntimeException {
    private static final String NOT_FOUND_BY_ID = "Cannot find a FhirLibrary for Id: %s";
    private static final String NOT_FOUND_BY_INCLUDE = "Cannot find a FhirLibrary for IncludeProperties: %s";

    public FhirLibraryNotFoundException(String measureId) {
        super(String.format(NOT_FOUND_BY_ID, measureId));
        log.warn(getMessage());
    }

    public FhirLibraryNotFoundException(IncludeProperties include) {
        super(String.format(NOT_FOUND_BY_INCLUDE, include == null ? "null" : include.toString()));
        log.warn(getMessage());
    }
}
