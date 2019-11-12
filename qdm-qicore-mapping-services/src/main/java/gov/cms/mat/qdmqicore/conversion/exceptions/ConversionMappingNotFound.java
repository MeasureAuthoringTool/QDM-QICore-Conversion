package gov.cms.mat.qdmqicore.conversion.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
@Slf4j
public class ConversionMappingNotFound extends RuntimeException {
    private static final String MESSAGE = "Cannot find ConversionMapping with name: %s and description: %s.";

    public ConversionMappingNotFound(String matAttributeName, String matDataTypeDescription) {
        super(String.format(MESSAGE, matAttributeName, matDataTypeDescription));
        log.warn(getMessage());
    }
}
