package gov.cms.mat.fhir.services.components.mat;

import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.ResourceFileUtil;
import gov.cms.mat.fhir.services.components.reporting.ConversionReporter;
import gov.cms.mat.fhir.services.components.reporting.ConversionResult;
import gov.cms.mat.fhir.services.components.reporting.ConversionResultsService;
import gov.cms.mat.fhir.services.components.reporting.ThreadSessionKey;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.MatXmlMarshalException;
import mat.client.measure.ManageCompositeMeasureDetailModel;
import mat.client.measurepackage.MeasurePackageDetail;
import mat.model.cql.CQLDefinitionsWrapper;
import mat.model.cql.CQLQualityDataModelWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class MatXmlMarshallerTest implements ResourceFileUtil {
    @Mock
    private ConversionResultsService conversionResultsService;

    @InjectMocks
    private MatXmlMarshaller matXmlMarshaller;

    private ThreadSessionKey threadSessionKey;

    @BeforeEach
    void setUp() {
        matXmlMarshaller = new MatXmlMarshaller();

        threadSessionKey = ConversionReporter.setInThreadLocal("1",
                "2",
                conversionResultsService,
                Instant.now(),
                ConversionType.CONVERSION,
                XmlSource.SIMPLE,
                true,
                null);
    }

    @Test
    void toCQLDefinitions_ErrorIsBlankCheck() {
        Exception exception = Assertions.assertThrows(MatXmlMarshalException.class, () -> {
            matXmlMarshaller.toCQLDefinitionsSupplementalData(null);
        });

        assertTrue(exception.getMessage().endsWith(", conversion result is null."));
    }

    @Test
    void toCQLDefinitions_ErrorWithNullXmlSource() {
        when(conversionResultsService.findConversionResult(threadSessionKey)).thenReturn(new ConversionResult());

        Exception exception = Assertions.assertThrows(MatXmlMarshalException.class, () -> {
            matXmlMarshaller.toCQLDefinitionsSupplementalData(null);
        });

        assertTrue(exception.getMessage().endsWith(", xmlSource is null."));
    }

    @Test
    void toCQLDefinitions_ErrorWithXmlSource() {
        ConversionResult conversionResult = new ConversionResult();
        conversionResult.setXmlSource(XmlSource.SIMPLE);
        when(conversionResultsService.findConversionResult(threadSessionKey)).thenReturn(conversionResult);

        Exception exception = Assertions.assertThrows(MatXmlMarshalException.class, () -> {
            matXmlMarshaller.toCQLDefinitionsSupplementalData(null);
        });

        assertTrue(exception.getMessage().endsWith(", using xmlSource: SIMPLE"));
    }

    @Test
    void toCQLDefinitions_ErrorIsInvalidXML() {
        Assertions.assertThrows(MatXmlMarshalException.class, () -> {
            matXmlMarshaller.toCQLDefinitionsSupplementalData("Bozo|T|Clown");
        });
    }

    @Test
    void toCQLDefinitions_Success() {
        String xml = getStringFromResource("/supplementalDataElements.xml");
        CQLDefinitionsWrapper model = matXmlMarshaller.toCQLDefinitionsSupplementalData(xml);
        assertEquals(5, model.getCqlDefinitions().size());
    }

    @Test
    void toCompositeMeasureDetail_Success() {
        String xml = getStringFromResource("/measureDetail.xml");
        ManageCompositeMeasureDetailModel model = matXmlMarshaller.toCompositeMeasureDetail(xml);
        assertNotNull(model.getId());
        assertEquals("Proportion", model.getMeasScoring());
    }

    @Test
    void toCompositeMeasureDetail_ErrorIsBlankCheck() {
        Assertions.assertThrows(MatXmlMarshalException.class, () -> {
            matXmlMarshaller.toCompositeMeasureDetail("");
        });
    }

    @Test
    void toCompositeMeasureDetail_ErrorIsInvalidXML() {
        Assertions.assertThrows(MatXmlMarshalException.class, () -> {
            matXmlMarshaller.toCompositeMeasureDetail("{\"json\": \"howdy\"}");
        });
    }

    @Test
    void toQualityData_Success() {
        String xml = getStringFromResource("/cqlLookUp.xml");
        CQLQualityDataModelWrapper model = matXmlMarshaller.toQualityData(xml);
        assertFalse(model.getQualityDataDTO().isEmpty());
    }

    @Test
    void toQualityData_ErrorIsBlankCheck() {
        Assertions.assertThrows(MatXmlMarshalException.class, () -> {
            matXmlMarshaller.toQualityData(null);
        });
    }

    @Test
    void toQualityData_ErrorIsInvalidXML() {
        Assertions.assertThrows(MatXmlMarshalException.class, () -> {
            matXmlMarshaller.toQualityData("BR-549 x234");
        });
    }

    @Test
    void toCQLDefinitionsRiskAdjustments() {
        String xml = getStringFromResource("/riskAdjustmentElements.xml");
        CQLDefinitionsWrapper model = matXmlMarshaller.toCQLDefinitionsRiskAdjustments(xml);
        assertEquals(2, model.getRiskAdjVarDTOList().size());
    }

    @Test
    void toMeasureGrouping() {
        String xml = getStringFromResource("/measureGrouping.xml");
        MeasurePackageDetail model = matXmlMarshaller.toMeasureGrouping(xml);
        assertEquals("1", model.getSequence());
        assertEquals(9, model.getPackageClauses().size());
    }
}