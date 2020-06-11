package gov.cms.mat.fhir.services.components.mat;

import gov.cms.mat.fhir.rest.dto.ConversionOutcome;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResult;
import gov.cms.mat.fhir.services.exceptions.MatXmlMarshalException;
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
public class MatXmlMarshaller {
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
            message = processErrorMessage(message);

            ConversionReporter.setTerminalMessage(message, ConversionOutcome.MEASURE_XML_NOT_FOUND);
            throw new MatXmlMarshalException(message);
        }
    }

    private String processErrorMessage(String message) {
        ConversionResult conversionResult = ConversionReporter.getConversionResult();

        if (conversionResult == null) {
            message = message + ", conversion result is null.";
        } else if (conversionResult.getXmlSource() == null) {
            message = message + ", xmlSource is null.";
        } else {
            message = message + ", using xmlSource: " + conversionResult.getXmlSource();
        }

        return message;
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
            throw new MatXmlMarshalException(e);
        }
    }

    private CQLDefinitionsWrapper convertCQLDefinitionsRiskAdjustments(String xml) {
        try {
            final String fileName = "CQLDefinitionsToRiskAdjustVariables.xml";
            return (CQLDefinitionsWrapper) xmlMarshalUtil.convertXMLToObject(fileName,
                    xml,
                    CQLDefinitionsWrapper.class);
        } catch (Exception e) {
            throw new MatXmlMarshalException(e);
        }
    }

    private ManageCompositeMeasureDetailModel convertCompositeMeasureDetail(String xml) {
        try {
            final String fileName = "CompositeMeasureDetailsModelMapping.xml";
            return (ManageCompositeMeasureDetailModel) xmlMarshalUtil.convertXMLToObject(fileName,
                    xml,
                    ManageCompositeMeasureDetailModel.class);

        } catch (Exception e) {
            log.error("error in convertCompositeMeasureDetail:",e);
            throw new MatXmlMarshalException(e);
        }
    }

    private CQLQualityDataModelWrapper convertQualityXml(String xml) {
        try {
            return (CQLQualityDataModelWrapper) xmlMarshalUtil.convertXMLToObject("CQLValueSetsMapping.xml",
                    xml,
                    CQLQualityDataModelWrapper.class);
        } catch (Exception e) {
            throw new MatXmlMarshalException(e);
        }
    }
}
