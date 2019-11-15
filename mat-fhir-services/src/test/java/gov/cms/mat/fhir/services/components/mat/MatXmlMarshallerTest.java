package gov.cms.mat.fhir.services.components.mat;

import gov.cms.mat.fhir.services.ResourceFileUtil;
import mat.client.measure.ManageCompositeMeasureDetailModel;
import mat.client.measurepackage.MeasurePackageDetail;
import mat.model.cql.CQLDefinitionsWrapper;
import mat.model.cql.CQLQualityDataModelWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.UncheckedIOException;

import static org.junit.jupiter.api.Assertions.*;

class MatXmlMarshallerTest implements ResourceFileUtil {
    private MatXmlMarshaller matXmlMarshaller;

    @BeforeEach
    void setUp() {
        matXmlMarshaller = new MatXmlMarshaller();
    }

    @Test
    void toCQLDefinitions_ErrorIsBlankCheck() {
        Assertions.assertThrows(UncheckedIOException.class, () -> {
            matXmlMarshaller.toCQLDefinitionsSupplementalData(null);
        });
    }

    @Test
    void toCQLDefinitions_ErrorIsInvalidXML() {
        Assertions.assertThrows(UncheckedIOException.class, () -> {
            matXmlMarshaller.toCQLDefinitionsSupplementalData("Bozo|T|Clown");
        });
    }

    @Test
    void toCQLDefinitions_Success() {
        String xml = getXml("/supplementalDataElements.xml");
        CQLDefinitionsWrapper model = matXmlMarshaller.toCQLDefinitionsSupplementalData(xml);
        assertEquals(5, model.getCqlDefinitions().size());
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

    @Test
    void toCQLDefinitionsRiskAdjustments() {
        String xml = getXml("/riskAdjustmentElements.xml");
        CQLDefinitionsWrapper model = matXmlMarshaller.toCQLDefinitionsRiskAdjustments(xml);
        assertEquals(2, model.getRiskAdjVarDTOList().size());
    }

    @Test
    void toMeasureGrouping() {
        String xml = getXml("/measureGrouping.xml");
        MeasurePackageDetail model = matXmlMarshaller.toMeasureGrouping(xml);
        assertEquals("1", model.getSequence());
        assertEquals(7, model.getPackageClauses().size());
    }
}