package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
@Slf4j
public class HumanReadableInvalidException extends RuntimeException {
    private static final String HUMAN_READABLE_MISSING_MESSAGE = "Human readable bytes are missing for measure: %s";
    private static final String EXCEPTION_MESSAGE = "Cannot process Human readable for id: %s: xhtml: %s";

    public HumanReadableInvalidException(String id) {
        super(String.format(HUMAN_READABLE_MISSING_MESSAGE, id));
        log.warn(getMessage());
    }

    public HumanReadableInvalidException(String id, String xhtml, Exception cause) {
        super(String.format(EXCEPTION_MESSAGE, id, xhtml), cause);
        log.warn(getMessage(), cause);
    }

}
