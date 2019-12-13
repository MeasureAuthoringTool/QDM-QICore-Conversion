package gov.cms.mat.cql_elm_translation.cql_translator;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class TranslationFailureException extends RuntimeException {
    public TranslationFailureException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
