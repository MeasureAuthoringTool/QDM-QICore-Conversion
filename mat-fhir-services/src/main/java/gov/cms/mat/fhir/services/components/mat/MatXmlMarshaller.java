package gov.cms.mat.fhir.services.components.mat;

import lombok.extern.slf4j.Slf4j;
import mat.client.measure.ManageCompositeMeasureDetailModel;
import mat.client.measurepackage.MeasurePackageDetail;
import mat.model.cql.CQLDefinitionsWrapper;
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
        checkXML(xml, "Mat CompositeMeasure Xml is blank");
        return convertCompositeMeasureDetail(xml);
    }

    CQLQualityDataModelWrapper toQualityData(String xml) {
        checkXML(xml, "Mat Quality Xml is blank");
        return convertQualityXml(xml);
    }

    public CQLDefinitionsWrapper toCQLDefinitionsSupplementalData(String xml) {
        checkXML(xml, "Mat supplementalDataElements Xml is blank");
        return convertCQLDefinitionsSupplementalData(xml);
    }

    CQLDefinitionsWrapper toCQLDefinitionsRiskAdjustments(String xml) {
        checkXML(xml, "Mat riskAdjustmentVariables Xml is blank");
        return convertCQLDefinitionsRiskAdjustments(xml);
    }

    MeasurePackageDetail toMeasureGrouping(String xml) {
        checkXML(xml, "Mat measureGrouping Xml is blank");
        return convertMeasureGrouping(xml);
    }

    private void checkXML(String xml, String message) {
        if (StringUtils.isBlank(xml)) {
            log.warn(message);
            throw new UncheckedIOException(new IOException(message));
        }
    }

    private MeasurePackageDetail convertMeasureGrouping(String xml) {
        try {
            final String fileName = "MeasurePackageClauseDetail.xml";
            return (MeasurePackageDetail) xmlMarshalUtil.convertXMLToObject(fileName,
                    xml,
                    MeasurePackageDetail.class);

        } catch (Exception e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }

    private CQLDefinitionsWrapper convertCQLDefinitionsSupplementalData(String xml) {
        try {
            final String fileName = "DefinitionToSupplementalDataElements.xml";
            return (CQLDefinitionsWrapper) xmlMarshalUtil.convertXMLToObject(fileName,
                    xml,
                    CQLDefinitionsWrapper.class);
        } catch (Exception e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }

    private CQLDefinitionsWrapper convertCQLDefinitionsRiskAdjustments(String xml) {
        try {
            final String fileName = "CQLDefinitionsToRiskAdjustVariables.xml";
            return (CQLDefinitionsWrapper) xmlMarshalUtil.convertXMLToObject(fileName,
                    xml,
                    CQLDefinitionsWrapper.class);
        } catch (Exception e) {
            throw new UncheckedIOException(new IOException(e));
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
