package gov.cms.mat.fhir.services.components.fhir;

import gov.cms.mat.fhir.rest.dto.ValueSetConversionResults;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResult;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.ValueSetValidationException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.summary.FhirValueSetResourceValidationResult;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValueSetFhirValidationResultsTest {
    private static final String MEASURE_ID = "measure_id";

    @Mock
    private HapiFhirServer hapiFhirServer;
    @Mock
    private ConversionResultsService conversionResultsService;

    @InjectMocks
    private ValueSetFhirValidationResults valueSetFhirValidationResults;

    @Test
    void generate_NoValueSets() {
        Assertions.assertThrows(ValueSetValidationException.class, () -> {
            valueSetFhirValidationResults.generate(Collections.emptyList(), XmlSource.SIMPLE, MEASURE_ID);
        });

        verifyNoInteractions(hapiFhirServer);
    }

    @Test
    void generate_WithValueSet_NoConversionResults() {
        ConversionReporter.setInThreadLocal(MEASURE_ID, conversionResultsService);

        ConversionResult conversionResult = new ConversionResult();

        when(conversionResultsService.findConversionResult(MEASURE_ID)).thenReturn(conversionResult);

        // when(hapiFhirServer.getCtx()).thenReturn(FhirContext.forR4());

        ValueSet valueSet = new ValueSet();
        valueSet.setId(MEASURE_ID);

        FhirValueSetResourceValidationResult fhirValueSetResourceValidationResult =
                valueSetFhirValidationResults.generate(Collections.singletonList(valueSet), XmlSource.SIMPLE, MEASURE_ID);

        assertEquals(XmlSource.SIMPLE, fhirValueSetResourceValidationResult.getXmlSource());
        assertTrue(fhirValueSetResourceValidationResult.getValueSetConversionResults().isEmpty());
        assertNull(fhirValueSetResourceValidationResult.getValueSetConversionType());

        //verify(hapiFhirServer).getCtx();
        verify(conversionResultsService).findConversionResult(MEASURE_ID);
    }

    @Test
    void generate_WithValueSet_HappyPath() {
        ConversionReporter.setInThreadLocal(MEASURE_ID, conversionResultsService);

        ConversionResult conversionResult = new ConversionResult();
        ValueSetConversionResults valueSetConversionResults = new ValueSetConversionResults();


        when(conversionResultsService.findConversionResult(MEASURE_ID)).thenReturn(conversionResult);

        // when(hapiFhirServer.getCtx()).thenReturn(FhirContext.forR4());

        ValueSet valueSet = new ValueSet();
        valueSet.setId(MEASURE_ID);

        FhirValueSetResourceValidationResult fhirValueSetResourceValidationResult =
                valueSetFhirValidationResults.generate(Collections.singletonList(valueSet), XmlSource.SIMPLE, MEASURE_ID);

        assertEquals(XmlSource.SIMPLE, fhirValueSetResourceValidationResult.getXmlSource());
        //   assertEquals(ConversionType.VALIDATION, fhirValueSetResourceValidationResult.getValueSetConversionType());

        // verify(hapiFhirServer).getCtx();
        verify(conversionResultsService).findConversionResult(MEASURE_ID);
    }


}