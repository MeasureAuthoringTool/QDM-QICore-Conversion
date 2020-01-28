package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.PAYLOAD_TOO_LARGE)
@Slf4j
public class ConversionResultsTooLargeException extends RuntimeException {
    private static final String MESSAGE = "Too many results, max: %s, found: %s";

    public ConversionResultsTooLargeException(int found, int max) {
        super(String.format(MESSAGE, found, max));
        log.info(getMessage());
    }
}
