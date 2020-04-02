package gov.cms.mat.cql_elm_translation.service.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.cql_elm_translation.ResourceFileUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnnotationErrorFilterTest implements ResourceFileUtil {
    private static final String CQL = "library URI_HEDIS_2020 version '1.1.000'\n";
    private static final String WARN_TAG = "\"errorSeverity\" : \"warn\",";
    private String sourceJson;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        sourceJson = getData("/fhir4_std_lib_errors_annotations.json");
        ObjectMapper objectMapper = new ObjectMapper();
        sourceJson = objectMapper.readTree(sourceJson).toPrettyString();
    }

    @Test
    void filter_VerifyException() {
        sourceJson = null;

        verifySourceAndCleanedTheSame();
    }

    @Test
    void filter_VerifyNoAnnotations() {
        sourceJson = getData("/fhir4_std_lib_no_annotations.json");

        verifySourceAndCleanedTheSame();
    }


    @Test
    void filter_VerifyEmptyAnnotations() {
        sourceJson = getData("/fhir4_std_lib_empty_array_annotations.json");

        verifySourceAndCleanedTheSame();
    }

    private void verifySourceAndCleanedTheSame() {
        AnnotationErrorFilter annotationErrorFilter = new AnnotationErrorFilter(CQL, false, sourceJson);
        String cleanedJson = annotationErrorFilter.filter();
        assertEquals(sourceJson, cleanedJson);
    }

    @Test
    void filter_VerifyErrorCleanUp() {
        String errorTag = "\"errorSeverity\" : \"error\"";
        String cleanedTag = "\"errorSeverity\" : \"Error\""; // after jackson toPrettyString()

        assertTrue(sourceJson.contains(errorTag));
        assertTrue(sourceJson.contains(WARN_TAG));

        AnnotationErrorFilter annotationErrorFilter = new AnnotationErrorFilter(CQL, false, sourceJson);
        String cleanedJson = annotationErrorFilter.filter();

        assertFalse(cleanedJson.contains(errorTag));
        assertTrue(cleanedJson.contains(cleanedTag));
        assertFalse(cleanedJson.contains(WARN_TAG));
    }

    @Test
    void filter_VerifyWarningStays() {
        assertTrue(sourceJson.contains(WARN_TAG));
        AnnotationErrorFilter annotationErrorFilter = new AnnotationErrorFilter(CQL, true, sourceJson);
        String cleanedJson = annotationErrorFilter.filter();

        assertTrue(cleanedJson.contains(WARN_TAG));
    }

    @Test
    void filter_verifyLibrary() {
        String tag = "\"libraryId\" : \"unknown\"";

        AnnotationErrorFilter annotationErrorFilter = new AnnotationErrorFilter(CQL, false, sourceJson);
        assertTrue(sourceJson.contains(tag));
        String cleanedJson = annotationErrorFilter.filter();
        assertFalse(cleanedJson.contains(tag));
    }
}