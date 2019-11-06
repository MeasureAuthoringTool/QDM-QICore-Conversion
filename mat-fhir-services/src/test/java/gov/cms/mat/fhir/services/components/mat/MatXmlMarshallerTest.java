package gov.cms.mat.fhir.services.components.mat;

import gov.cms.mat.fhir.services.ResourceFileUtil;
import mat.client.measure.ManageCompositeMeasureDetailModel;
import mat.model.cql.CQLQualityDataModelWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.UncheckedIOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MatXmlMarshallerTest implements ResourceFileUtil {
    private MatXmlMarshaller matXmlMarshaller;

    @BeforeEach
    void setUp() {
        matXmlMarshaller = new MatXmlMarshaller();
    }

    @Test
    void toCompositeMeasureDetail_Success() {
        String xml = getXml("/measureDetail.xml");
        ManageCompositeMeasureDetailModel model = matXmlMarshaller.toCompositeMeasureDetail(xml);
        assertNotNull(model.getId());
    }

    @Test
    void toCompositeMeasureDetail_ErrorIsBlankCheck() {
        Assertions.assertThrows(UncheckedIOException.class, () -> {
            matXmlMarshaller.toCompositeMeasureDetail("");
        });
    }

    @Test
    void toCompositeMeasureDetail_ErrorIsInvalidXML() {
        Assertions.assertThrows(UncheckedIOException.class, () -> {
            matXmlMarshaller.toCompositeMeasureDetail("{\"json\": \"howdy\"}");
        });
    }

    @Test
    void toQualityData_Success() {
        String xml = getXml("/cqlLookUp.xml");
        CQLQualityDataModelWrapper model = matXmlMarshaller.toQualityData(xml);
        assertFalse(model.getQualityDataDTO().isEmpty());
    }

    @Test
    void toQualityData_ErrorIsBlankCheck() {
        Assertions.assertThrows(UncheckedIOException.class, () -> {
            matXmlMarshaller.toQualityData(null);
        });
    }

    @Test
    void toQualityData_ErrorIsInvalidXML() {
        Assertions.assertThrows(UncheckedIOException.class, () -> {
            matXmlMarshaller.toQualityData("BR-549 x234");
        });
    }
}