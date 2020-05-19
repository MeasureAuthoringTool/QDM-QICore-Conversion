package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
@Slf4j
public class FhirIncludeLibrariesNotFoundException extends RuntimeException {
    private static final String NOT_FOUND_BY_ID = "Cannot find included FhirLibrary for Id: %s,  Libraries: %s";

    public FhirIncludeLibrariesNotFoundException(String measureId, String libs) {
        super(String.format(NOT_FOUND_BY_ID, measureId, libs));
        log.warn(getMessage());
    }
}
