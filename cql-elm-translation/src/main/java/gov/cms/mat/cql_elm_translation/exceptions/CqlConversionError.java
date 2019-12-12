package gov.cms.mat.cql_elm_translation.exceptions;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
@Slf4j
public class CqlConversionError extends RuntimeException {
    @Getter
    private final List<String> errors;

    public CqlConversionError(List<String> errors) {
        super(String.join(",/n", errors));
        this.errors = errors;
        log.warn(getMessage());
    }
}
