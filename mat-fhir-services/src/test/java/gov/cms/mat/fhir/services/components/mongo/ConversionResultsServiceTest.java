package gov.cms.mat.fhir.services.components.mongo;


import gov.cms.mat.fhir.rest.dto.FieldConversionResult;
import gov.cms.mat.fhir.rest.dto.ValueSetResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversionResultsServiceTest {
    private static final String MEASURE_ID = "measureId";
    private static final String MAT_LIBRARY_ID = "matLibraryId";

    private ConversionKey conversionKey;


    @Mock
    private ConversionResultRepository conversionResultRepository;
    @InjectMocks
    private ConversionResultsService conversionResultsService;

    @BeforeEach
    public void setUp() {
        conversionKey = ConversionKey.builder()
                .measureId(MEASURE_ID)
                .start(Instant.now())
                .build();
    }


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
        when(conversionResultRepository.findByMeasureIdAndStart(conversionKey.getMeasureId(),
                conversionKey.getStart())).thenReturn(Optional.empty());
        ConversionResult conversionResultToReturn = new ConversionResult();

        when(conversionResultRepository.save(any(ConversionResult.class)))
                .thenReturn(conversionResultToReturn);

        FieldConversionResult result = buildMeasureResult();

        conversionResultsService.addMeasureResult(conversionKey, result);

        verify(conversionResultRepository).findByMeasureIdAndStart(conversionKey.getMeasureId(), conversionKey.getStart());
        verify(conversionResultRepository).save(any(ConversionResult.class));
    }

    @Test
    void addLibraryResult_NotFoundInDb() {
        when(conversionResultRepository.findByMeasureIdAndStart(conversionKey.getMeasureId(), conversionKey.getStart()))
                .thenReturn(Optional.empty());
        ConversionResult conversionResultToReturn = new ConversionResult();

        when(conversionResultRepository.save(any(ConversionResult.class)))
                .thenReturn(conversionResultToReturn);

        FieldConversionResult result = buildLibraryResult();

        conversionResultsService.addLibraryFieldConversionResult(conversionKey, result, MAT_LIBRARY_ID);

        verify(conversionResultRepository).findByMeasureIdAndStart(conversionKey.getMeasureId(), conversionKey.getStart());
        verify(conversionResultRepository).save(any(ConversionResult.class));
    }


    @Test
    void addMeasureResult_FoundInDb() {
        ConversionResult conversionResultToReturn = new ConversionResult();
        when(conversionResultRepository.findByMeasureIdAndStart(conversionKey.getMeasureId(), conversionKey.getStart()))
                .thenReturn(Optional.of(conversionResultToReturn));

        when(conversionResultRepository.save(conversionResultToReturn))
                .thenReturn(conversionResultToReturn);

        FieldConversionResult result = buildMeasureResult();

        conversionResultsService.addMeasureResult(conversionKey, result);


        verify(conversionResultRepository).findByMeasureIdAndStart(conversionKey.getMeasureId(), conversionKey.getStart());
        verify(conversionResultRepository).save(conversionResultToReturn);
    }

    @Test
    void addLibraryResult_FoundInDb() {
        ConversionResult conversionResultToReturn = new ConversionResult();
        when(conversionResultRepository.findByMeasureIdAndStart(conversionKey.getMeasureId(), conversionKey.getStart()))
                .thenReturn(Optional.of(conversionResultToReturn));

        when(conversionResultRepository.save(conversionResultToReturn))
                .thenReturn(conversionResultToReturn);

        FieldConversionResult result = buildLibraryResult();

        conversionResultsService.addLibraryFieldConversionResult(conversionKey, result, MAT_LIBRARY_ID);


        verify(conversionResultRepository).findByMeasureIdAndStart(conversionKey.getMeasureId(), conversionKey.getStart());
        verify(conversionResultRepository).save(conversionResultToReturn);
    }

    @Test
    void addValueSetResult_NotFoundInDb() {
        when(conversionResultRepository.findByMeasureIdAndStart(conversionKey.getMeasureId(), conversionKey.getStart()))
                .thenReturn(Optional.empty());
        ConversionResult conversionResultToReturn = new ConversionResult();

        when(conversionResultRepository.save(any(ConversionResult.class)))
                .thenReturn(conversionResultToReturn);

        conversionResultsService.addValueSetResult(conversionKey, "oid", "reason", true, null);


        verify(conversionResultRepository).findByMeasureIdAndStart(conversionKey.getMeasureId(), conversionKey.getStart());
        verify(conversionResultRepository).save(any(ConversionResult.class));
    }


    @Test
    void addValueSetResult_FoundInDb() {
        ConversionResult conversionResultToReturn = new ConversionResult();
        when(conversionResultRepository.findByMeasureIdAndStart(conversionKey.getMeasureId(), conversionKey.getStart()))
                .thenReturn(Optional.of(conversionResultToReturn));

        when(conversionResultRepository.save(conversionResultToReturn))
                .thenReturn(conversionResultToReturn);

        ValueSetResult result = buildValueSetResult();

        conversionResultsService.addValueSetResult(conversionKey, "oid", "reason", Boolean.TRUE, null);

        verify(conversionResultRepository).findByMeasureIdAndStart(conversionKey.getMeasureId(), conversionKey.getStart());
        verify(conversionResultRepository).save(conversionResultToReturn);
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
                .reason("REASON")
                .build();
    }
}