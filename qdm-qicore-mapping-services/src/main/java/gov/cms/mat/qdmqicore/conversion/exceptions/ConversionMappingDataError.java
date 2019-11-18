package gov.cms.mat.qdmqicore.conversion.exceptions;

import gov.cms.mat.qdmqicore.conversion.data.SearchData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
@Slf4j
public class ConversionMappingDataError extends RuntimeException {
    private static final String MESSAGE = "Expecting one ConversionMapping, found %d with searchData: %s";

    public ConversionMappingDataError(SearchData searchData, int count) {
        super(String.format(MESSAGE, count, searchData));
        log.warn(getMessage());
    }
}
