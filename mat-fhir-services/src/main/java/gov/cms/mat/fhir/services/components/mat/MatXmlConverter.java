package gov.cms.mat.fhir.services.components.mat;

import mat.client.measure.ManageCompositeMeasureDetailModel;
import mat.client.measurepackage.MeasurePackageDetail;
import mat.model.cql.CQLDefinitionsWrapper;
import mat.model.cql.CQLQualityDataModelWrapper;
import org.springframework.stereotype.Component;

@Component
public class MatXmlConverter {
    private final MatXpath matXpath;
    private final MatXmlMarshaller matXmlMarshaller;

    public MatXmlConverter(MatXpath matXpath, MatXmlMarshaller matXmlMarshaller) {
        this.matXpath = matXpath;
        this.matXmlMarshaller = matXmlMarshaller;
    }

    public MeasurePackageDetail toMeasureGrouping(String xml) {
        try {
            String xpathXml = matXpath.toMeasureGrouping(xml);
            return matXmlMarshaller.toMeasureGrouping(xpathXml);
        } catch (Exception e) {
            throw new MatXmlException(e);
        }
    }


    public ManageCompositeMeasureDetailModel toCompositeMeasureDetail(String xml) {
        try {
            String xpathXml = matXpath.toCompositeMeasureDetail(xml);
            return matXmlMarshaller.toCompositeMeasureDetail(xpathXml);
        } catch (Exception e) {
            throw new MatXmlException(e);
        }
    }

    public CQLQualityDataModelWrapper toQualityData(String xml) {
        try {
            String xpathXml = matXpath.toQualityData(xml);
            return matXmlMarshaller.toQualityData(xpathXml);
        } catch (Exception e) {
            throw new MatXmlException(e);
        }
    }

    public CQLDefinitionsWrapper toCQLDefinitionsSupplementalData(String xml) {
        try {
            String xpathXml = matXpath.toCQLDefinitionsSupplementalData(xml);
            return matXmlMarshaller.toCQLDefinitionsSupplementalData(xpathXml);
        } catch (Exception e) {
            throw new MatXmlException(e);
        }
    }

    CQLDefinitionsWrapper toCQLDefinitionsRiskAdjustments(String xml) {
        try {
            String xpathXml = matXpath.toCQLDefinitionsRiskAdjustments(xml);
            return matXmlMarshaller.toCQLDefinitionsRiskAdjustments(xpathXml);
        } catch (Exception e) {
            throw new MatXmlException(e);
        }
    }


}
