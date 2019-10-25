package mat.server;

import mat.client.measure.ManageCompositeMeasureDetailModel;
import mat.client.measure.ManageMeasureDetailModel;
import mat.server.service.impl.XMLMarshalUtil;
import mat.server.util.XmlProcessor;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;


public class MeasureLibraryService {

    private final static javax.xml.xpath.XPath xPath = XPathFactory.newInstance().newXPath();

    public static ManageCompositeMeasureDetailModel createModelFromXML(String xml) throws IOException {
        try {
            XmlProcessor processor = new XmlProcessor(xml);
            String componentMeasuresXPath = "/measure/measureDetails";
            Node measureDetailsNode = (Node) xPath.evaluate(componentMeasuresXPath, processor.getOriginalDoc().getDocumentElement(), XPathConstants.NODE);
            String measureDetailsXML = processor.transform(measureDetailsNode);

            XMLMarshalUtil xmlMarshalUtil = new XMLMarshalUtil();

            return (ManageCompositeMeasureDetailModel) xmlMarshalUtil.convertXMLToObject("CompositeMeasureDetailsModelMapping.xml", measureDetailsXML, ManageCompositeMeasureDetailModel.class);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

//    public static ManageMeasureDetailModel createModelFromXML(String xml) throws IOException {
//        return createObjectFromXMLMapping(xml, "MeasureDetailsModelMapping.xml", ManageMeasureDetailModel.class);
//    }

//    public static ManageMeasureDetailModel createModelFromXML(String xml) throws IOException {
//        return createObjectFromXMLMapping(xml, "MeasureDetailsModelMapping.xml", ManageMeasureDetailModel.class);
//    }


    private static ManageMeasureDetailModel createObjectFromXMLMapping(String xml, String xmlMapping, Class<?> clazz) throws IOException {

        Object result = null;
        XMLMarshalUtil xmlMarshalUtil = new XMLMarshalUtil();
        try {
            result = xmlMarshalUtil.convertXMLToObject(xmlMapping, xml, clazz);

        } catch (MarshalException | ValidationException | MappingException | IOException e) {
            throw new IOException(e);
        }
        return (ManageMeasureDetailModel) result;
    }


//    private void createMeasureDetailsModelForCompositeMeasure(final ManageCompositeMeasureDetailModel model, final Measure measure) {
//        createMeasureDetailsModelFromMeasure(model, measure);
//        model.setCompositeScoringMethod(measure.getCompositeScoring());
//        model.setQdmVersion(measure.getQdmVersion());
//        List<ManageMeasureSearchModel.Result> componentMeasuresSelectedList = new ArrayList<>();
//        Map<String, String> aliasMapping = new HashMap<>();
//        boolean isSuperUser = LoggedInUserUtil.getLoggedInUserRole().equalsIgnoreCase(SecurityRole.SUPER_USER_ROLE);
//        for(ComponentMeasure component : measure.getComponentMeasures()) {
//            componentMeasuresSelectedList.add(buildSearchModelResultObjectFromMeasureId(component.getComponentMeasure().getId(), isSuperUser));
//        }
//        model.setAppliedComponentMeasures(componentMeasuresSelectedList);
//        model.setComponentMeasuresSelectedList(componentMeasuresSelectedList);
//
//        for(ComponentMeasure component : measure.getComponentMeasures() ) {
//            aliasMapping.put(component.getComponentMeasure().getId(), component.getAlias());
//        }
//
//        model.setAliasMapping(aliasMapping);
//    }


}
