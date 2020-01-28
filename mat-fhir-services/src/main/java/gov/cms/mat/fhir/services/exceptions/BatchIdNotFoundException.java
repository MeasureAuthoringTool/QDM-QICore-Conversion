package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
@Slf4j
public class BatchIdNotFoundException extends RuntimeException {
    private static final String MESSAGE = "No documents found for BatchId: %s";

    public BatchIdNotFoundException(String batchId) {
        super(String.format(MESSAGE, batchId));
        log.info(getMessage());
    }
}
