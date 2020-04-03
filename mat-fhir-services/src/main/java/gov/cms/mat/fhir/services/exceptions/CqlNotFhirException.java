package gov.cms.mat.fhir.services.exceptions;

import gov.cms.mat.cql.elements.UsingProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
@Slf4j
public class CqlNotFhirException extends RuntimeException {
    private static final String ERROR_MESSAGE = "Cql is not FHIR: %s";

    public CqlNotFhirException(UsingProperties usingProperties) {
        super(String.format(ERROR_MESSAGE, usingProperties == null ? "null" : usingProperties.toString()));
        log.warn(getMessage());
    }
}
