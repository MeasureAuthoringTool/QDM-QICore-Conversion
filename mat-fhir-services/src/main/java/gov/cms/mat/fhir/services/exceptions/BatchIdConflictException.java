package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
@Slf4j
public class BatchIdConflictException extends RuntimeException {
    private static final String MESSAGE = "BatchId is has already been used: %s";

    public BatchIdConflictException(String batchId) {
        super(String.format(MESSAGE, batchId));
        log.info(getMessage());
    }
}
