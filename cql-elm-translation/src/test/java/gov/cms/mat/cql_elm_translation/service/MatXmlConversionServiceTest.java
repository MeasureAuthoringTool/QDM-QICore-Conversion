package gov.cms.mat.cql_elm_translation.service;

import gov.cms.mat.cql_elm_translation.ResourceFileUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatXmlConversionServiceTest implements ResourceFileUtil {
    MatXmlConversionService matXmlConversionService;

    @BeforeEach
    void setUp() {
        matXmlConversionService = new MatXmlConversionService();
    }

    @Test
    void processCalXml_SendEmptyXml() {
        String xml = "<xml/>";
        String cql = matXmlConversionService.processCqlXml(xml);
        assertEquals("context Patient\n\n", cql);
    }

    @Test
    void processCalXml_SendValidXml() {
        String xml = getData("/test_mat_cql.xml");
        String cql = matXmlConversionService.processCqlXml(xml);
        assertTrue(cql.startsWith("library THKR version '0.0.000'"));
    }
}
