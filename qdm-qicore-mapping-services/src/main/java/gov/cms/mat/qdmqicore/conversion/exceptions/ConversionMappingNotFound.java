package gov.cms.mat.qdmqicore.conversion.exceptions;

import gov.cms.mat.qdmqicore.conversion.data.SearchData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
@Slf4j
public class ConversionMappingNotFound extends RuntimeException {
    private static final String MESSAGE = "Cannot find ConversionMapping with searchData: %s";

    public ConversionMappingNotFound(SearchData searchData) {
        super(String.format(MESSAGE, searchData));
        log.warn(getMessage());
    }
}
