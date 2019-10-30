package gov.cms.mat.fhir.services.components.mat;

import mat.client.measure.ManageCompositeMeasureDetailModel;
import mat.model.cql.CQLQualityDataModelWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.xpath.XPathExpressionException;

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
    void toCompositeMeasureDetail_ThrowsException() throws XPathExpressionException {
        when(matXpath.toCompositeMeasureDetail(XML)).thenThrow(new IllegalArgumentException("oops"));

        Assertions.assertThrows(MatXmlException.class, () -> {
            matXmlConverter.toCompositeMeasureDetail(XML);
        });

        verify(matXpath).toCompositeMeasureDetail(XML);
    }

    @Test
    void toCompositeMeasureDetail_HappyPath() throws XPathExpressionException {
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

        Assertions.assertThrows(MatXmlException.class, () -> {
            matXmlConverter.toQualityData(XML);
        });

        verify(matXpath).toQualityData(XML);
    }

    @Test
    void toQualityData_HappyPath() throws XPathExpressionException {
        CQLQualityDataModelWrapper modelToReturn = new CQLQualityDataModelWrapper();

        when(matXpath.toQualityData(XML)).thenReturn(XPATH);
        when(matXmlMarshaller.toQualityData(XPATH)).thenReturn(modelToReturn);

        CQLQualityDataModelWrapper modelReturned = matXmlConverter.toQualityData(XML);

        assertEquals(modelToReturn, modelReturned);

        verify(matXpath).toQualityData(XML);
        verify(matXmlMarshaller).toQualityData(XPATH);
    }
}