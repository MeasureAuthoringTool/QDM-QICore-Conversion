package gov.cms.mat.fhir.services.components.mat;

import gov.cms.mat.fhir.services.ResourceFileUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.xpath.XPathExpressionException;

import static gov.cms.mat.fhir.services.components.mat.MatXpath.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatXpathTest implements ResourceFileUtil {
    private static final String XML_START = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><%s>";

    private MatXpath matXpath;
    private String xml;

    @BeforeEach
    void setUp() {
        matXpath = new MatXpath();
        xml = getXml("/measureExportSimple.xml");

        String expectedBaseXmlBeforeXpath = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<measure>";
        assertTrue(xml.startsWith(expectedBaseXmlBeforeXpath));
    }

    @Test
    void toCompositeMeasureDetail() throws XPathExpressionException {
        String xpath = matXpath.toCompositeMeasureDetail(xml);
        assertTrue(xpath.startsWith(createExpectedPath(MEASURE_DETAILS_TAG)));
    }

    @Test
    void toQualityData() {
        String xpath = matXpath.toQualityData(xml);
        assertTrue(xpath.startsWith(createExpectedPath(CQL_LOOK_UP_TAG)));
    }

    @Test
    void toCQLDefinitionsSupplementalData() {
        String xpath = matXpath.toCQLDefinitionsSupplementalData(xml);
        assertTrue(xpath.startsWith(createExpectedPath(SUPPLEMENTAL_DATA_TAG)));
    }

    @Test
    void toCQLDefinitionsRiskAdjustments() {
        String xpath = matXpath.toCQLDefinitionsRiskAdjustments(xml);
        assertTrue(xpath.startsWith(createExpectedPath(RISK_ADJUSTMENTS_TAG)));
    }

    @Test
    void toMeasureGrouping() {
        String xpath = matXpath.toMeasureGrouping(xml);
        assertTrue(xpath.startsWith(createExpectedPath(MEASURE_GROUPING_TAG)));
    }

    private String createExpectedPath(String tag) {
        return String.format(XML_START, tag);
    }
}