package gov.cms.mat.fhir.services.components.mongo;


import gov.cms.mat.fhir.rest.cql.FieldConversionResult;
import gov.cms.mat.fhir.rest.cql.ValueSetResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void findAll() {
        ConversionResult conversionResult = new ConversionResult();

        when(conversionResultRepository.findAll(any(Sort.class)))
                .thenReturn(Collections.singletonList(conversionResult));

        List<ConversionResult> conversionResults = conversionResultsService.findAll();

        assertEquals(1, conversionResults.size());
        assertEquals(conversionResult, conversionResults.get(0));

        verify(conversionResultRepository).findAll(any(Sort.class));
    }

    @Test
    void addMeasureResult_NotFoundInDb() {
        when(conversionResultRepository.findByMeasureId(MEASURE_ID)).thenReturn(Optional.empty());
        ConversionResult conversionResultToReturn = new ConversionResult();

        when(conversionResultRepository.save(any(ConversionResult.class)))
                .thenReturn(conversionResultToReturn);

        FieldConversionResult result = buildMeasureResult();

        ConversionResult conversionResultReturned = conversionResultsService.addMeasureResult(MEASURE_ID, result);

        assertEquals(conversionResultToReturn, conversionResultReturned);

        verify(conversionResultRepository).findByMeasureId(MEASURE_ID);
        verify(conversionResultRepository).save(any(ConversionResult.class));
    }

    @Test
    void addLibraryResult_NotFoundInDb() {
        when(conversionResultRepository.findByMeasureId(MEASURE_ID)).thenReturn(Optional.empty());
        ConversionResult conversionResultToReturn = new ConversionResult();

        when(conversionResultRepository.save(any(ConversionResult.class)))
                .thenReturn(conversionResultToReturn);

        FieldConversionResult result = buildLibraryResult();

        ConversionResult conversionResultReturned = conversionResultsService.addLibraryResult(MEASURE_ID, result);

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

        FieldConversionResult result = buildMeasureResult();

        ConversionResult conversionResultReturned = conversionResultsService.addMeasureResult(MEASURE_ID, result);

        assertEquals(conversionResultToReturn, conversionResultReturned);

        verify(conversionResultRepository).findByMeasureId(MEASURE_ID);
        verify(conversionResultRepository).save(conversionResultToReturn);
    }

    @Test
    void addLibraryResult_FoundInDb() {
        ConversionResult conversionResultToReturn = new ConversionResult();
        when(conversionResultRepository.findByMeasureId(MEASURE_ID))
                .thenReturn(Optional.of(conversionResultToReturn));

        when(conversionResultRepository.save(conversionResultToReturn))
                .thenReturn(conversionResultToReturn);

        FieldConversionResult result = buildLibraryResult();

        ConversionResult conversionResultReturned = conversionResultsService.addLibraryResult(MEASURE_ID, result);

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

        ValueSetResult result = buildValueSetResult();

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

        ValueSetResult result = buildValueSetResult();

        ConversionResult conversionResultReturned = conversionResultsService.addValueSetResult(MEASURE_ID, result);

        assertEquals(conversionResultToReturn, conversionResultReturned);

        verify(conversionResultRepository).findByMeasureId(MEASURE_ID);
        verify(conversionResultRepository).save(conversionResultToReturn);
    }

    @Test
    void clearValueSetResults_NotFoundInDb() {
        when(conversionResultRepository.findByMeasureId(MEASURE_ID)).thenReturn(Optional.empty());

        conversionResultsService.clearValueSetResults(MEASURE_ID);

        verify(conversionResultRepository).findByMeasureId(MEASURE_ID);
        verify(conversionResultRepository, never()).save(any(ConversionResult.class));
    }

    @Test
    void clearValueSetResults_FoundInDb() {
        ConversionResult conversionResultToReturn = new ConversionResult();
        when(conversionResultRepository.findByMeasureId(MEASURE_ID))
                .thenReturn(Optional.of(conversionResultToReturn));

        conversionResultsService.clearValueSetResults(MEASURE_ID);
        verify(conversionResultRepository).findByMeasureId(MEASURE_ID);
    }

    @Test
    void clearMeasure_NotFoundInDb() {
        when(conversionResultRepository.findByMeasureId(MEASURE_ID)).thenReturn(Optional.empty());

        conversionResultsService.clearMeasure(MEASURE_ID);

        verify(conversionResultRepository).findByMeasureId(MEASURE_ID);
        verify(conversionResultRepository, never()).save(any(ConversionResult.class));
    }

    @Test
    void clearLibrary_NotFoundInDb() {

        when(conversionResultRepository.findByMeasureId(MEASURE_ID)).thenReturn(Optional.empty());

        conversionResultsService.clearLibrary(MEASURE_ID);

        verify(conversionResultRepository).findByMeasureId(MEASURE_ID);
        verify(conversionResultRepository, never()).save(any(ConversionResult.class));
    }


    @Test
    void clearMeasure_FoundInDb() {
        ConversionResult conversionResultToReturn = new ConversionResult();
        when(conversionResultRepository.findByMeasureId(MEASURE_ID))
                .thenReturn(Optional.of(conversionResultToReturn));


        conversionResultsService.clearMeasure(MEASURE_ID);

        verify(conversionResultRepository).findByMeasureId(MEASURE_ID);
    }

    @Test
    void clearLibrary_FoundInDb() {
        ConversionResult conversionResultToReturn = new ConversionResult();
        when(conversionResultRepository.findByMeasureId(MEASURE_ID))
                .thenReturn(Optional.of(conversionResultToReturn));


        conversionResultsService.clearLibrary(MEASURE_ID);

        verify(conversionResultRepository).findByMeasureId(MEASURE_ID);
    }


    private FieldConversionResult buildMeasureResult() {
        return FieldConversionResult.builder()
                .field("FIELD")
                .destination("DESTINATION")
                .reason("REASON")
                .build();
    }

    private FieldConversionResult buildLibraryResult() {
        return FieldConversionResult.builder()
                .field("FIELD")
                .destination("DESTINATION")
                .reason("REASON")
                .build();
    }

    private ValueSetResult buildValueSetResult() {
        return ValueSetResult.builder()
                .oid("OID")
                .reason("REASON")
                .build();
    }
}