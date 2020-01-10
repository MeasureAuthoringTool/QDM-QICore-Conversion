package gov.cms.mat.cql_elm_translation.service.support;

import gov.cms.mat.cql_elm_translation.ResourceFileUtil;
import org.cqframework.cql.cql2elm.CqlTranslatorException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CqlExceptionErrorProcessorTest implements ResourceFileUtil {
    private CqlExceptionErrorProcessor cqlExceptionErrorProcessor;

    @Test
    void process_EmptyErrors() {
        String json = getData("/library-elm.json");
        cqlExceptionErrorProcessor = new CqlExceptionErrorProcessor(Collections.emptyList(), json);

        assertEquals(json, cqlExceptionErrorProcessor.process()); // since no errors no changes to json
    }

    @Test
    void process_WithError() {
        String json = getData("/library-elm.json");

        List<CqlTranslatorException> errors = Collections.singletonList(createError());

        cqlExceptionErrorProcessor = new CqlExceptionErrorProcessor(errors, json);

        assertNotEquals(json, cqlExceptionErrorProcessor.process());
    }

    private CqlTranslatorException createError() {
        return new CqlTranslatorException("message", CqlTranslatorException.ErrorSeverity.Error);

    }


}