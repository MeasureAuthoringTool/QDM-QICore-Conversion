package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.LOCKED)
@Slf4j
public class OrchestrationBatchJobAlreadyRunningException extends RuntimeException {
    private static final String MESSAGE = "Orchestration Batch Job Already Running for batchId: %s, running seconds: %d";

    public OrchestrationBatchJobAlreadyRunningException(String batchId, Long seconds) {
        super(String.format(MESSAGE, batchId, seconds));
        log.info(getMessage());
    }
}
