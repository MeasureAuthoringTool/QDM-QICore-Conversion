package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import gov.cms.mat.fhir.services.translate.ValueSetMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValueSetControllerTest {
    @Mock
    private MeasureExportRepository measureExportRepository;
    @Mock
    private ValueSetMapper valueSetMapper;

    @InjectMocks
    private ValueSetController valueSetController;

    @Test
    void translateAll() {
        //todo MCG complete
        valueSetController.translateAll();
    }

    @Test
    void countValueSets() {
        when(valueSetMapper.count()).thenReturn(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, valueSetMapper.count());
    }

    @Test
    void deleteValueSets() {
        when(valueSetMapper.deleteAll()).thenReturn(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, valueSetMapper.deleteAll());
    }
}