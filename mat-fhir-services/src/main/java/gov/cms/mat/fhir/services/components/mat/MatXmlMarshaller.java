package gov.cms.mat.fhir.services.components.mat;

import lombok.extern.slf4j.Slf4j;
import mat.client.measure.ManageCompositeMeasureDetailModel;
import mat.model.cql.CQLQualityDataModelWrapper;
import mat.server.service.impl.XMLMarshalUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;

@Component
@Slf4j
class MatXmlMarshaller {
    private final XMLMarshalUtil xmlMarshalUtil = new XMLMarshalUtil();

    ManageCompositeMeasureDetailModel toCompositeMeasureDetail(String xml) {
        if (StringUtils.isBlank(xml)) {
            String message = "Mat CompositeMeasure Xml is blank";
            log.warn(message);
            throw new UncheckedIOException(new IOException(message));
        } else {
            return convertCompositeMeasureDetail(xml);
        }
    }

    CQLQualityDataModelWrapper toQualityData(String xml) {
        if (StringUtils.isBlank(xml)) {
            String message = "Mat Quality Xml is blank";
            log.warn(message);
            throw new UncheckedIOException(new IOException(message));
        } else {
            return convertQualityXml(xml);
        }
    }

    private ManageCompositeMeasureDetailModel convertCompositeMeasureDetail(String xml) {
        try {
            final String fileName = "CompositeMeasureDetailsModelMapping.xml";
            return (ManageCompositeMeasureDetailModel) xmlMarshalUtil.convertXMLToObject(fileName,
                    xml,
                    ManageCompositeMeasureDetailModel.class);
        } catch (Exception e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }

    private CQLQualityDataModelWrapper convertQualityXml(String xml) {
        try {
            return (CQLQualityDataModelWrapper) xmlMarshalUtil.convertXMLToObject("CQLValueSetsMapping.xml",
                    xml,
                    CQLQualityDataModelWrapper.class);
        } catch (Exception e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }
}
