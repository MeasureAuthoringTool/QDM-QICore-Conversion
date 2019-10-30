package mat.server;

import mat.client.measure.ManageCompositeMeasureDetailModel;
import mat.server.service.impl.XMLMarshalUtil;
import mat.server.util.XmlProcessor;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;


public class MeasureLibraryService {

    private final static javax.xml.xpath.XPath xPath = XPathFactory.newInstance().newXPath();

    public static ManageCompositeMeasureDetailModel createModelFromXML(String xml) throws IOException {
        try {
            String measureDetailsXML = getMeasureDetailsXpathXml(xml);

            XMLMarshalUtil xmlMarshalUtil = new XMLMarshalUtil();

            return (ManageCompositeMeasureDetailModel) xmlMarshalUtil.convertXMLToObject("CompositeMeasureDetailsModelMapping.xml", measureDetailsXML, ManageCompositeMeasureDetailModel.class);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private static String getMeasureDetailsXpathXml(String xml) throws XPathExpressionException {
        XmlProcessor processor = new XmlProcessor(xml);
        String componentMeasuresXPath = "/measure/measureDetails";

        Node measureDetailsNode =
                (Node) xPath.evaluate(componentMeasuresXPath,
                        processor.getOriginalDoc().getDocumentElement(),
                        XPathConstants.NODE);

        return processor.transform(measureDetailsNode);
    }


}
