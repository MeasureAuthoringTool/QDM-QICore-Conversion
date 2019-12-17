package gov.cms.mat.cql_elm_translation.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
@Slf4j
public class QdmVersionNotfound extends RuntimeException {
    public QdmVersionNotfound() {
        super("Qdm version not found in cql");
        log.warn(getMessage());
    }
}
