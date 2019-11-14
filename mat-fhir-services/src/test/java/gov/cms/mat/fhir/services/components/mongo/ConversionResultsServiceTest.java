package gov.cms.mat.fhir.services.components.mongo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversionResultsServiceTest {
    private static final String MEASURE_ID = "measureId";

    @Mock
    private ConversionResultRepository conversionResultRepository;
    @InjectMocks
    private ConversionResultsService conversionResultsService;

    @Test
    void addMeasureResult_NotFoundInDb() {
        when(conversionResultRepository.findByMeasureId(MEASURE_ID)).thenReturn(Optional.empty());
        ConversionResult conversionResultToReturn = new ConversionResult();

        when(conversionResultRepository.save(any(ConversionResult.class)))
                .thenReturn(conversionResultToReturn);

        ConversionResult.MeasureResult result = buildMeasureResult();

        ConversionResult conversionResultReturned = conversionResultsService.addMeasureResult(MEASURE_ID, result);

        assertEquals(conversionResultToReturn, conversionResultReturned);

        verify(conversionResultRepository).findByMeasureId(MEASURE_ID);
        verify(conversionResultRepository).save(any(ConversionResult.class));
    }

    @Test
    void addMeasureResult_FoundInDb() {
        ConversionResult conversionResultToReturn = new ConversionResult();
        when(conversionResultRepository.findByMeasureId(MEASURE_ID))
                .thenReturn(Optional.of(conversionResultToReturn));

        when(conversionResultRepository.save(conversionResultToReturn))
                .thenReturn(conversionResultToReturn);

        ConversionResult.MeasureResult result = buildMeasureResult();

        ConversionResult conversionResultReturned = conversionResultsService.addMeasureResult(MEASURE_ID, result);

        assertEquals(conversionResultToReturn, conversionResultReturned);

        verify(conversionResultRepository).findByMeasureId(MEASURE_ID);
        verify(conversionResultRepository).save(conversionResultToReturn);
    }


    @Test
    void addValueSetResult_NotFoundInDb() {
        when(conversionResultRepository.findByMeasureId(MEASURE_ID)).thenReturn(Optional.empty());
        ConversionResult conversionResultToReturn = new ConversionResult();

        when(conversionResultRepository.save(any(ConversionResult.class)))
                .thenReturn(conversionResultToReturn);

        ConversionResult.ValueSetResult result = buildValueSetResult();

        ConversionResult conversionResultReturned = conversionResultsService.addValueSetResult(MEASURE_ID, result);

        assertEquals(conversionResultToReturn, conversionResultReturned);

        verify(conversionResultRepository).findByMeasureId(MEASURE_ID);
        verify(conversionResultRepository).save(any(ConversionResult.class));
    }


    @Test
    void addValueSetResult_FoundInDb() {
        ConversionResult conversionResultToReturn = new ConversionResult();
        when(conversionResultRepository.findByMeasureId(MEASURE_ID))
                .thenReturn(Optional.of(conversionResultToReturn));

        when(conversionResultRepository.save(conversionResultToReturn))
                .thenReturn(conversionResultToReturn);

        ConversionResult.ValueSetResult result = buildValueSetResult();

        ConversionResult conversionResultReturned = conversionResultsService.addValueSetResult(MEASURE_ID, result);

        assertEquals(conversionResultToReturn, conversionResultReturned);

        verify(conversionResultRepository).findByMeasureId(MEASURE_ID);
        verify(conversionResultRepository).save(conversionResultToReturn);
    }

    @Test
    void clearValueSetResults_NotFoundInDb() {
        when(conversionResultRepository.findByMeasureId(MEASURE_ID)).thenReturn(Optional.empty());

        assertNull(conversionResultsService.clearValueSetResults(MEASURE_ID));

        verify(conversionResultRepository).findByMeasureId(MEASURE_ID);
        verify(conversionResultRepository, never()).save(any(ConversionResult.class));
    }

    @Test
    void clearValueSetResults_FoundInDb() {
        ConversionResult conversionResultToReturn = new ConversionResult();
        when(conversionResultRepository.findByMeasureId(MEASURE_ID))
                .thenReturn(Optional.of(conversionResultToReturn));

        when(conversionResultRepository.save(conversionResultToReturn))
                .thenReturn(conversionResultToReturn);

        ConversionResult conversionResultReturned =
                conversionResultsService.clearValueSetResults(MEASURE_ID);

        assertEquals(conversionResultToReturn, conversionResultReturned);

        verify(conversionResultRepository).findByMeasureId(MEASURE_ID);
        verify(conversionResultRepository).save(conversionResultReturned);
    }

    @Test
    void clearMeasure_NotFoundInDb() {

        when(conversionResultRepository.findByMeasureId(MEASURE_ID)).thenReturn(Optional.empty());

        assertNull(conversionResultsService.clearMeasure(MEASURE_ID));

        verify(conversionResultRepository).findByMeasureId(MEASURE_ID);
        verify(conversionResultRepository, never()).save(any(ConversionResult.class));
    }

    @Test
    void clearMeasure_FoundInDb() {

        ConversionResult conversionResultToReturn = new ConversionResult();
        when(conversionResultRepository.findByMeasureId(MEASURE_ID))
                .thenReturn(Optional.of(conversionResultToReturn));

        when(conversionResultRepository.save(conversionResultToReturn))
                .thenReturn(conversionResultToReturn);

        ConversionResult conversionResultReturned =
                conversionResultsService.clearMeasure(MEASURE_ID);

        assertEquals(conversionResultToReturn, conversionResultReturned);

        verify(conversionResultRepository).findByMeasureId(MEASURE_ID);
        verify(conversionResultRepository).save(conversionResultReturned);
    }

    private ConversionResult.MeasureResult buildMeasureResult() {
        return ConversionResult.MeasureResult.builder()
                .field("FIELD")
                .reason("REASON")
                .build();
    }

    private ConversionResult.ValueSetResult buildValueSetResult() {
        return ConversionResult.ValueSetResult.builder()
                .oid("OID")
                .reason("REASON")
                .build();
    }
}