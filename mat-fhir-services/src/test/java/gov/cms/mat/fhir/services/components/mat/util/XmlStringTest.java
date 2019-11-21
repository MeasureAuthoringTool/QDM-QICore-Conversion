package gov.cms.mat.fhir.services.components.mat.util;

import gov.cms.mat.fhir.services.ResourceFileUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XmlStringTest implements ResourceFileUtil {

    private String xml;

    @BeforeEach
    void setUp() {
        xml = getXml("/measureGrouping.xml");
    }

    @Test
    void process() {
        XmlString xmlString = new XmlString(xml);
        List<String> list = xmlString.process();
        assertEquals(4, list.size());
    }
}