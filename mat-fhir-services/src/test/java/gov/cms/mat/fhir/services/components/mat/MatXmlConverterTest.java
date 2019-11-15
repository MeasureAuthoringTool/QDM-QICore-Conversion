package gov.cms.mat.fhir.services.components.mat;

import mat.client.measure.ManageCompositeMeasureDetailModel;
import mat.client.measurepackage.MeasurePackageDetail;
import mat.model.cql.CQLDefinitionsWrapper;
import mat.model.cql.CQLQualityDataModelWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatXmlConverterTest {
    private static final String XML = "xml";
    private static final String XPATH = "xpath";

    @Mock
    private MatXpath matXpath;
    @Mock
    private MatXmlMarshaller matXmlMarshaller;
    @InjectMocks
    private MatXmlConverter matXmlConverter;

    @Test
    void toCompositeMeasureDetail_ThrowsException() {
        when(matXpath.toCompositeMeasureDetail(XML)).thenThrow(new IllegalArgumentException("oops"));

        Assertions.assertThrows(MatXmlException.class, () -> matXmlConverter.toCompositeMeasureDetail(XML));

        verify(matXpath).toCompositeMeasureDetail(XML);
    }

    @Test
    void toCompositeMeasureDetail_HappyPath() {
        ManageCompositeMeasureDetailModel modelToReturn = new ManageCompositeMeasureDetailModel();

        when(matXpath.toCompositeMeasureDetail(XML)).thenReturn(XPATH);
        when(matXmlMarshaller.toCompositeMeasureDetail(XPATH)).thenReturn(modelToReturn);

        ManageCompositeMeasureDetailModel modelReturned = matXmlConverter.toCompositeMeasureDetail(XML);

        assertEquals(modelToReturn, modelReturned);

        verify(matXpath).toCompositeMeasureDetail(XML);
        verify(matXmlMarshaller).toCompositeMeasureDetail(XPATH);
    }

    @Test
    void toQualityData_ThrowsException() {
        when(matXpath.toQualityData(XML)).thenThrow(new IllegalArgumentException("my bad"));

        Assertions.assertThrows(MatXmlException.class, () -> matXmlConverter.toQualityData(XML));

        verify(matXpath).toQualityData(XML);
    }

    @Test
    void toQualityData_HappyPath() {
        CQLQualityDataModelWrapper modelToReturn = new CQLQualityDataModelWrapper();

        when(matXpath.toQualityData(XML)).thenReturn(XPATH);
        when(matXmlMarshaller.toQualityData(XPATH)).thenReturn(modelToReturn);

        CQLQualityDataModelWrapper modelReturned = matXmlConverter.toQualityData(XML);

        assertEquals(modelToReturn, modelReturned);

        verify(matXpath).toQualityData(XML);
        verify(matXmlMarshaller).toQualityData(XPATH);
    }

    @Test
    void toCQLDefinitionsSupplementalData_ThrowsException() {
        when(matXpath.toCQLDefinitionsSupplementalData(XML)).thenThrow(new IllegalArgumentException("sky is falling"));

        Assertions.assertThrows(MatXmlException.class, () -> matXmlConverter.toCQLDefinitionsSupplementalData(XML));

        verify(matXpath).toCQLDefinitionsSupplementalData(XML);
    }

    @Test
    void toCQLDefinitionsSupplementalData_HappyPath() {
        CQLDefinitionsWrapper modelToReturn = new CQLDefinitionsWrapper();

        when(matXpath.toCQLDefinitionsSupplementalData(XML)).thenReturn(XPATH);
        when(matXmlMarshaller.toCQLDefinitionsSupplementalData(XPATH)).thenReturn(modelToReturn);

        CQLDefinitionsWrapper modelReturned = matXmlConverter.toCQLDefinitionsSupplementalData(XML);

        assertEquals(modelToReturn, modelReturned);

        verify(matXpath).toCQLDefinitionsSupplementalData(XML);
        verify(matXmlMarshaller).toCQLDefinitionsSupplementalData(XPATH);
    }

    @Test
    void toCQLDefinitionsRiskAdjustments_ThrowsException() {
        when(matXpath.toCQLDefinitionsRiskAdjustments(XML)).thenThrow(new IllegalArgumentException("we need a bigger boat"));

        Assertions.assertThrows(MatXmlException.class, () -> matXmlConverter.toCQLDefinitionsRiskAdjustments(XML));

        verify(matXpath).toCQLDefinitionsRiskAdjustments(XML);
    }

    @Test
    void toCQLDefinitionsRiskAdjustments_HappyPath() {
        CQLDefinitionsWrapper modelToReturn = new CQLDefinitionsWrapper();

        when(matXpath.toCQLDefinitionsRiskAdjustments(XML)).thenReturn(XPATH);
        when(matXmlMarshaller.toCQLDefinitionsRiskAdjustments(XPATH)).thenReturn(modelToReturn);

        CQLDefinitionsWrapper modelReturned = matXmlConverter.toCQLDefinitionsRiskAdjustments(XML);

        assertEquals(modelToReturn, modelReturned);

        verify(matXpath).toCQLDefinitionsRiskAdjustments(XML);
        verify(matXmlMarshaller).toCQLDefinitionsRiskAdjustments(XPATH);
    }

    @Test
    void toMeasureGrouping_ThrowsException() {
        when(matXpath.toMeasureGrouping(XML)).thenThrow(new IllegalArgumentException("Picked the wrong week"));

        Assertions.assertThrows(MatXmlException.class, () -> matXmlConverter.toMeasureGrouping(XML));

        verify(matXpath).toMeasureGrouping(XML);
    }

    @Test
    void toMeasureGrouping_HappyPath() {
        MeasurePackageDetail modelToReturn = new MeasurePackageDetail();

        when(matXpath.toMeasureGrouping(XML)).thenReturn(XPATH);
        when(matXmlMarshaller.toMeasureGrouping(XPATH)).thenReturn(modelToReturn);

        MeasurePackageDetail modelReturned = matXmlConverter.toMeasureGrouping(XML);

        assertEquals(modelToReturn, modelReturned);

        verify(matXpath).toMeasureGrouping(XML);
        verify(matXmlMarshaller).toMeasureGrouping(XPATH);
    }
}