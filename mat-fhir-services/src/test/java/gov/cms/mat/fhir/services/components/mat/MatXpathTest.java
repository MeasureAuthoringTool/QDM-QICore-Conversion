package gov.cms.mat.fhir.services.components.mat;

import gov.cms.mat.fhir.services.ResourceFileUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MatXpathTest implements ResourceFileUtil {
    private MatXpath matXpath;
    private String xml;

    @BeforeEach
    void setUp() throws IOException {
        matXpath = new MatXpath();
        xml = getXml("/measureExportSimple.xml");
    }

    @Test
    void toCompositeMeasureDetail_Verify() throws XPathExpressionException {
        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<measure>\n";
        assertTrue(xml.startsWith(expectedXml));

        String expectedXpath = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><measureDetails>\n";

        String xpath = matXpath.toCompositeMeasureDetail(xml);
        assertTrue(xpath.startsWith(expectedXpath));
    }

    @Test
    void toQualityData() {
        String expectedXpath = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><cqlLookUp>\n";
        String xpath = matXpath.toQualityData(xml);
        assertTrue(xpath.startsWith(expectedXpath));
    }
}