package gov.cms.mat.qdmqicore.conversion.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
@Slf4j
public class ConversionMappingDataError extends RuntimeException {
    private static final String MESSAGE = "Expecting one ConversionMapping, found %d with name: %s and description: %s.";

    public ConversionMappingDataError(String matAttributeName, String matDataTypeDescription, int count) {
        super(String.format(MESSAGE, count, matAttributeName, matDataTypeDescription));
        log.warn(getMessage());
    }
}
