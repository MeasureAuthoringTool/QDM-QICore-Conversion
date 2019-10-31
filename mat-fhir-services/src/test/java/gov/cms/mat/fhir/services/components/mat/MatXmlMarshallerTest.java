package gov.cms.mat.fhir.services.components.mat;

import gov.cms.mat.fhir.services.ResourceFileUtil;
import mat.client.measure.ManageCompositeMeasureDetailModel;
import mat.model.cql.CQLQualityDataModelWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MatXmlMarshallerTest implements ResourceFileUtil {
    private MatXmlMarshaller matXmlMarshaller;

    @BeforeEach
    void setUp() {
        matXmlMarshaller = new MatXmlMarshaller();
    }

    @Test
    void toCompositeMeasureDetail() {
        String xml = getXml("/measureDetail.xml");
        ManageCompositeMeasureDetailModel model = matXmlMarshaller.toCompositeMeasureDetail(xml);
        assertNotNull(model.getId());
    }

    @Test
    void toQualityData() {
        String xml = getXml("/cqlLookUp.xml");
        CQLQualityDataModelWrapper model = matXmlMarshaller.toQualityData(xml);
        assertFalse(model.getQualityDataDTO().isEmpty());
    }
}