package gov.cms.mat.cql_elm_translation.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
@Slf4j
public class QdmLibraryParseError extends RuntimeException {
    public QdmLibraryParseError(String libraryData) {
        super("Cql library data is malformed: " + libraryData);
        log.warn(getMessage());
    }
}
