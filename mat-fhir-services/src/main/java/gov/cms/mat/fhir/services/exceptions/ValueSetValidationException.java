package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
@Slf4j
public class ValueSetValidationException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "No value sets found for measure: %s ";

    public ValueSetValidationException(String measureId) {
        super(String.format(MESSAGE_TEMPLATE, measureId));
    }
}
