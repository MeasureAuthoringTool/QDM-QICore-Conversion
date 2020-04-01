package gov.cms.mat.cql_elm_translation.service.filters;

import gov.cms.mat.cql_elm_translation.ResourceFileUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnotationErrorFilterTest implements ResourceFileUtil {
    private static final String CQL = "library URI_HEDIS_2020 version '1.1.000'\n";
    private static final String PRE_TAG = "\"errorSeverity\": \"error\"";
    private static final String CLEAN_TAG = "\"errorSeverity\" : \"error\""; // after formatting

    private String sourceJson;

    @BeforeEach
    void setUp() {
        sourceJson = getData("/fhir4_std_lib_errors_annotations.json");
    }


    @Test
    void filter_VerifyErrorCleanUp() {
        AnnotationErrorFilter annotationErrorFilter = new AnnotationErrorFilter(CQL, false, sourceJson);
        assertTrue(sourceJson.contains(PRE_TAG));

        String cleanedJson = annotationErrorFilter.filter();

        assertFalse(cleanedJson.contains(CLEAN_TAG));
    }
}