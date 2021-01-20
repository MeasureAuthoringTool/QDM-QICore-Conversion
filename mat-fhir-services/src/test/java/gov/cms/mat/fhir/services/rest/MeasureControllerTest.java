package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.services.exceptions.HapiResourceNotFoundException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.translate.MeasureMapper;
import org.hl7.fhir.r4.model.Measure;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeasureControllerTest {
    private static final String ID = "id";

    @Mock
    private HapiFhirServer hapiFhirServer;
    @Mock
    private MeasureMapper measureMapper;

    @InjectMocks
    private MeasureController measureController;

    @Test
    void findOneNotFound() {
        when(hapiFhirServer.fetchHapiMeasure(ID)).thenReturn(Optional.empty());

        Assertions.assertThrows(HapiResourceNotFoundException.class, () -> {
            measureController.findOne(ID);
        });

        verifyNoMoreInteractions(hapiFhirServer);
        verifyNoInteractions(measureMapper);
    }

    @Test
    void findOne() {
        Measure measure = new Measure();
        when(hapiFhirServer.fetchHapiMeasure(ID)).thenReturn(Optional.of(measure));
        when(hapiFhirServer.toJson(measure)).thenReturn("JSON");

        assertEquals("JSON", measureController.findOne(ID));

        verifyNoInteractions(measureMapper);
    }

    @Test
    void countMeasures() {
        when(measureMapper.count()).thenReturn(Integer.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, measureController.countMeasures());

        verifyNoMoreInteractions(measureMapper);
        verifyNoInteractions(hapiFhirServer);
    }

    @Test
    void deleteMeasures() {
        when(measureMapper.deleteAll()).thenReturn(1);

        assertEquals(1, measureController.deleteMeasures());

        verifyNoMoreInteractions(measureMapper);
        verifyNoInteractions(hapiFhirServer);
    }
}