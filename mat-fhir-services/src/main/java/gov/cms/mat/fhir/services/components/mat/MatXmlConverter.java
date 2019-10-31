package gov.cms.mat.fhir.services.components.mat;

import mat.client.measure.ManageCompositeMeasureDetailModel;
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
}
