package gov.cms.mat.fhir.services.components.mat;

import gov.cms.mat.fhir.services.components.mat.util.XmlString;
import mat.client.measure.ManageCompositeMeasureDetailModel;
import mat.client.measurepackage.MeasurePackageDetail;
import mat.model.cql.CQLDefinitionsWrapper;
import mat.model.cql.CQLQualityDataModelWrapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MatXmlConverter {

    private final MatXpath matXpath;
    private final MatXmlMarshaller matXmlMarshaller;

    public MatXmlConverter(MatXpath matXpath, MatXmlMarshaller matXmlMarshaller) {
        this.matXpath = matXpath;
        this.matXmlMarshaller = matXmlMarshaller;
    }

    public List<MeasurePackageDetail> toMeasureGroupings(String xml) {
        List<String> data = new XmlString(xml).process();

        return data.stream()
                .map(this::toMeasureGrouping)
                .collect(Collectors.toList());
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
        } catch (MatXmlException e) {
            throw e;
        } catch (Exception  e) {
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

    public CQLDefinitionsWrapper toCQLDefinitionsRiskAdjustments(String xml) {
        try {
            String xpathXml = matXpath.toCQLDefinitionsRiskAdjustments(xml);
            return matXmlMarshaller.toCQLDefinitionsRiskAdjustments(xpathXml);
        } catch (Exception e) {
            throw new MatXmlException(e);
        }
    }
}
