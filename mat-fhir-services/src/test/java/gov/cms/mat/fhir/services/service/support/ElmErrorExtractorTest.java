package gov.cms.mat.fhir.services.service.support;

import gov.cms.mat.fhir.services.ResourceFileUtil;
import gov.cms.mat.fhir.services.exceptions.CqlConversionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ElmErrorExtractorTest implements ResourceFileUtil {
    @Test
    void parse_WithError() {
        String json = getStringFromResource("/cql-elm-with-errors.json");

        ElmErrorExtractor elmErrorExtractor = new ElmErrorExtractor(json);

        assertEquals(15, elmErrorExtractor.parseForAnnotations().size());
    }

    @Test
    void parse_WithOutErrors() {
        String json = getStringFromResource("/cql-elm.json");

        ElmErrorExtractor elmErrorExtractor = new ElmErrorExtractor(json);

        assertTrue(elmErrorExtractor.parseForAnnotations().isEmpty());
    }

    @Test
    void parse_BadJson() {
        String json = "<xml/>";

        ElmErrorExtractor elmErrorExtractor = new ElmErrorExtractor(json);

        CqlConversionException thrown = assertThrows(CqlConversionException.class, elmErrorExtractor::parseForAnnotations);

        assertTrue(thrown.getMessage().contains("Error processing json"));
    }
}