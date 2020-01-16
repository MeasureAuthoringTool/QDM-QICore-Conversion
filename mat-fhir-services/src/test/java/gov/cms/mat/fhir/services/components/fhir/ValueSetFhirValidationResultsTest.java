package gov.cms.mat.fhir.services.components.fhir;

import gov.cms.mat.fhir.services.components.mongo.ConversionKey;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.ValueSetValidationException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;

import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ValueSetFhirValidationResultsTest {
    private static final String MEASURE_ID = "measure_id";

    private ConversionKey conversionKey;

    @Mock
    private HapiFhirServer hapiFhirServer;
    @Mock
    private ConversionResultsService conversionResultsService;

    @InjectMocks
    private ValueSetFhirValidationResults valueSetFhirValidationResults;

    @BeforeEach
    public void setUp() {
        conversionKey = ConversionKey.builder()
                .measureId(MEASURE_ID)
                .start(Instant.now())
                .build();
    }

    @Test
    void generate_NoValueSets() {
        Assertions.assertThrows(ValueSetValidationException.class, () -> {
            valueSetFhirValidationResults.generate(Collections.emptyList(), XmlSource.SIMPLE, MEASURE_ID);
        });

        verifyNoInteractions(hapiFhirServer);
    }

    @Test
    void generate_WithValueSet_NoConversionResults() {
//        ConversionReporter.setInThreadLocal(MEASURE_ID, conversionResultsService, conversionKey.getStart());
//
//        ConversionResult conversionResult = new ConversionResult();
//
//       // when(conversionResultsService.findConversionResult(conversionKey)).thenReturn(conversionResult);
//
//        // when(hapiFhirServer.getCtx()).thenReturn(FhirContext.forR4());
//
//        ValueSet valueSet = new ValueSet();
//        valueSet.setId(MEASURE_ID);
//
//        FhirValueSetResourceValidationResult fhirValueSetResourceValidationResult =
//                valueSetFhirValidationResults.generate(Collections.singletonList(valueSet), XmlSource.SIMPLE, MEASURE_ID);
//
//        assertEquals(XmlSource.SIMPLE, fhirValueSetResourceValidationResult.getXmlSource());
//        assertTrue(fhirValueSetResourceValidationResult.getValueSetConversionResults().isEmpty());
//        assertNull(fhirValueSetResourceValidationResult.getValueSetConversionType());

        //verify(hapiFhirServer).getCtx();
        //  verify(conversionResultsService).findConversionResult(conversionKey);
    }

    @Test
    void generate_WithValueSet_HappyPath() {
//        ConversionReporter.setInThreadLocal(MEASURE_ID, conversionResultsService, conversionKey.getStart());
//
//        ConversionResult conversionResult = new ConversionResult();
//        ValueSetConversionResults valueSetConversionResults = new ValueSetConversionResults();
//
//        when(conversionResultsService.findConversionResult(conversionKey)).thenReturn(conversionResult);
//
//        // when(hapiFhirServer.getCtx()).thenReturn(FhirContext.forR4());
//
//        ValueSet valueSet = new ValueSet();
//        valueSet.setId(MEASURE_ID);
//
//        FhirValueSetResourceValidationResult fhirValueSetResourceValidationResult =
//                valueSetFhirValidationResults.generate(Collections.singletonList(valueSet), XmlSource.SIMPLE, MEASURE_ID);
//
//        assertEquals(XmlSource.SIMPLE, fhirValueSetResourceValidationResult.getXmlSource());
//        //   assertEquals(ConversionType.VALIDATION, fhirValueSetResourceValidationResult.getValueSetConversionType());
//
//        // verify(hapiFhirServer).getCtx();
//        verify(conversionResultsService).findConversionResult(conversionKey);
    }


}