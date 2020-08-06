package gov.cms.mat.fhir.services.exceptions;

import gov.cms.mat.fhir.services.components.reporting.ThreadSessionKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
@Slf4j
public class ConversionResultsNotFoundException extends RuntimeException {
    private static final String MESSAGE = "Cannot find ConversionResults with measureId: %s";

    public ConversionResultsNotFoundException(ThreadSessionKey key) {
        super(String.format(MESSAGE, key == null ? "null" : key.toString()));
        log.warn(getMessage());
    }
}
