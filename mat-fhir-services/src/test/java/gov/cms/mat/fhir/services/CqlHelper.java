package gov.cms.mat.fhir.services;

import mat.model.cql.CQLModel;
import mat.server.service.impl.XMLMarshalUtil;
import mat.server.util.XmlProcessor;

import java.io.IOException;
import java.io.UncheckedIOException;

public interface CqlHelper extends  ResourceFileUtil {

    default CQLModel loadMatXml(String fileName)  {
        String xml =  getStringFromResource(fileName);
        XmlProcessor measureXMLProcessor = new XmlProcessor(xml);
        String cqlXmlFrag = measureXMLProcessor.getXmlByTagName("cqlLookUp");

        XMLMarshalUtil xmlMarshalUtil = new XMLMarshalUtil();

        try {
            return (CQLModel) xmlMarshalUtil.convertXMLToObject("CQLModelMapping.xml", cqlXmlFrag, CQLModel.class);
        } catch (Exception e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }
}


