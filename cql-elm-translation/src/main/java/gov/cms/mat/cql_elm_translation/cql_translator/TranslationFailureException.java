/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.cql_elm_translation.cql_translator;

import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.cqframework.cql.elm.tracking.TrackBack;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/**
 * @author mhadley
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class TranslationFailureException extends RuntimeException {

    public TranslationFailureException(String msg) {
        super(msg);
    }

    public TranslationFailureException(List<CqlTranslatorException> translationErrs) {
        super(formatMsg(translationErrs));
    }

    private static String formatMsg(List<CqlTranslatorException> translationErrs) {
        StringBuilder msg = new StringBuilder();
        msg.append("Translation failed due to errors:");
        for (CqlTranslatorException error : translationErrs) {
            TrackBack tb = error.getLocator();
            String lines = tb == null ? "[n/a]" : String.format("[%d:%d, %d:%d]",
                    tb.getStartLine(), tb.getStartChar(), tb.getEndLine(),
                    tb.getEndChar());
            msg.append(String.format("%s %s%n", lines, error.getMessage()));
        }
        return msg.toString();
    }
}
