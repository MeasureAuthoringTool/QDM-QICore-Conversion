package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
@Slf4j
public class FhirNotUniqueException extends RuntimeException {
    private static final String ERROR_MESSAGE = "Fhir results are not uniq count: %d data: %s";

    public FhirNotUniqueException(String data, int count) {
        super(String.format(ERROR_MESSAGE, count, data));
        log.warn(getMessage());
    }
}
