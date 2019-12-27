package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.objects.TranslationOutcome;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.fhir.ValueSetFhirValidationResults;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.ValueSetConversionException;
import gov.cms.mat.fhir.services.service.ValueSetService;
import gov.cms.mat.fhir.services.summary.FhirValueSetResourceValidationResult;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValueSetControllerTest {
    private static final String MEASURE_ID = "measureId";

    @Mock
    private ValueSetService valueSetService;
    @Mock
    private ValueSetFhirValidationResults valueSetFhirValidationResults;

    @InjectMocks
    private ValueSetController valueSetController;

    @Test
    void translateAll() {
        String successMessage = "success";

        when(valueSetService.translateAll(XmlSource.SIMPLE, ConversionType.CONVERSION)).thenReturn(successMessage);

        TranslationOutcome translationOutcome = valueSetController.translateAll(XmlSource.SIMPLE, ConversionType.CONVERSION);
        assertEquals(successMessage, translationOutcome.getMessage());
    }

    @Test
    void validate_NoMeasure() {
        when(valueSetService.findValueSets(XmlSource.SIMPLE, MEASURE_ID, ConversionType.VALIDATION))
                .thenReturn(Collections.emptyList());

        assertThrows(ValueSetConversionException.class, () -> valueSetController.validate(XmlSource.SIMPLE, MEASURE_ID));

        verify(valueSetService).findValueSets(XmlSource.SIMPLE, MEASURE_ID, ConversionType.VALIDATION);
        verifyNoInteractions(valueSetFhirValidationResults);
    }

    @Test
    void validate_WithMeasure() {
        List<ValueSet> valueSets = Collections.singletonList(new ValueSet());
        when(valueSetService.findValueSets(XmlSource.SIMPLE, MEASURE_ID, ConversionType.VALIDATION))
                .thenReturn(valueSets);

        FhirValueSetResourceValidationResult expected = new FhirValueSetResourceValidationResult();
        when(valueSetFhirValidationResults.generate(valueSets, XmlSource.SIMPLE, MEASURE_ID)).thenReturn(expected);

        FhirValueSetResourceValidationResult returned = valueSetController.validate(XmlSource.SIMPLE, MEASURE_ID);
        assertEquals(expected, returned);

        verify(valueSetService).findValueSets(XmlSource.SIMPLE, MEASURE_ID, ConversionType.VALIDATION);
        verify(valueSetFhirValidationResults).generate(valueSets, XmlSource.SIMPLE, MEASURE_ID);
    }

    @Test
    void countValueSets() {
        when(valueSetService.count()).thenReturn(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, valueSetController.countValueSets());
    }

    @Test
    void deleteValueSets() {
        when(valueSetService.deleteAll()).thenReturn(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, valueSetController.deleteValueSets());
    }
}