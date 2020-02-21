package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.services.components.fhir.ValueSetFhirValidationResults;
import gov.cms.mat.fhir.services.service.ValueSetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

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