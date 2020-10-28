package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
@Slf4j
public class InvalidOrchestrationParametersException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "Cannot convert measures in a draft state.";

    public InvalidOrchestrationParametersException() {
        super(MESSAGE_TEMPLATE);
    }
}
