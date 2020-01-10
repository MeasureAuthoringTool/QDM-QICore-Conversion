package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
@Slf4j
public class MeasureNotFoundException extends RuntimeException {
    private static final String MESSAGE = "Cannot find MAT Measure with measureId: %s";

    public MeasureNotFoundException(String measureId) {
        super(String.format(MESSAGE, measureId));
        log.warn(getMessage());
    }
}
