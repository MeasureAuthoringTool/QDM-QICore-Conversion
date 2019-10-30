package gov.cms.mat.fhir.services.components.mat;

import mat.server.util.XmlProcessor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


@Component
class MatXpath {
    private static final String CQL_LOOKUP = "cqlLookUp";
    private final XPath xPath = XPathFactory.newInstance().newXPath();

    String toCompositeMeasureDetail(String xml) throws XPathExpressionException {
        XmlProcessor processor = new XmlProcessor(xml);
        String componentMeasuresXPath = "/measure/measureDetails";

        Node measureDetailsNode =
                (Node) xPath.evaluate(componentMeasuresXPath,
                        processor.getOriginalDoc().getDocumentElement(),
                        XPathConstants.NODE);

        return processor.transform(measureDetailsNode);
    }

    String toQualityData(String xml) {
        return new XmlProcessor(xml).getXmlByTagName(CQL_LOOKUP);
    }
}
