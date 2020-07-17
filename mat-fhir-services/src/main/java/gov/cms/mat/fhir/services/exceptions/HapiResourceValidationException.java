package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
@Slf4j
public class HapiResourceValidationException extends RuntimeException {
    private static final String ID_MESSAGE = "Could not validate hapi %s with id: %s";

    public HapiResourceValidationException(String id, String type) {
        super(String.format(ID_MESSAGE, type, id));
        log.warn(getMessage());
    }

    public HapiResourceValidationException(String errorMessage) {
        super(errorMessage);
        log.warn(getMessage());
    }
}
