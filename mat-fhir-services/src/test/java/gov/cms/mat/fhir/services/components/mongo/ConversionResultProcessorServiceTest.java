package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.dto.*;
import gov.cms.mat.fhir.services.exceptions.ConversionResultsNotFoundException;
import gov.cms.mat.fhir.services.service.QdmQiCoreDataService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversionResultProcessorServiceTest {
    private static final String MEASURE_ID = "measure_id";

    private ThreadSessionKey threadSessionKey;

    @Mock
    private QdmQiCoreDataService qdmQiCoreDataService;
    @Mock
    private ConversionResultsService conversionResultsService;

    @InjectMocks
    private ConversionResultProcessorService conversionResultProcessorService;

    @BeforeEach
    public void setUp() {
        threadSessionKey = ThreadSessionKey.builder()
                .measureId(MEASURE_ID)
                .start(Instant.now())
                .build();
    }


    @Test
    void processAll_HappyPath() {
        when(conversionResultsService.findAll())
                .thenReturn(Collections.singletonList(createConversionResult(true)));

        List<ConversionResultDto> conversionResults = conversionResultProcessorService.processAll();
        ConversionResultDto dto = verifyResults(conversionResults);

        verify(conversionResultsService).findAll();
    }

    @Test
    void findMissingValueSets() {
        when(conversionResultsService.findAll())
                .thenReturn(Collections.singletonList(createConversionResult(false)));

        Set<String> set = conversionResultProcessorService.findMissingValueSets();

        assertEquals(1, set.size());
      //  assertEquals("OID", set.iterator().next());
    }

    @Test
    void processSearchData_NotFound() {
        when(conversionResultsService.findByMeasureId(threadSessionKey)).thenReturn(Optional.empty());

        ConversionResultsNotFoundException thrown =
                Assertions.assertThrows(ConversionResultsNotFoundException.class, () -> {
                    conversionResultProcessorService.process(threadSessionKey);
                });

        assertTrue(thrown.getMessage().contains(MEASURE_ID));
        verify(conversionResultsService).findByMeasureId(threadSessionKey);
    }

    @Test
    void processSearchData_HappyPath() {
        when(conversionResultsService.findByMeasureId(threadSessionKey)).thenReturn(Optional.of(createConversionResult(true)));

        ConversionResultDto dto = conversionResultProcessorService.process(threadSessionKey);
        verifyResult(dto);
        // dto.getMeasureResults().forEach(r -> assertNull(r.getErrorMessage()));

        verify(conversionResultsService).findByMeasureId(threadSessionKey);

    }


    private ConversionResultDto verifyResults(List<ConversionResultDto> conversionResults) {
        assertEquals(1, conversionResults.size());

        ConversionResultDto dto = conversionResults.get(0);
        verifyResult(dto);

        return dto;
    }

    private void verifyResult(ConversionResultDto dto) {
        assertEquals(MEASURE_ID, dto.getMeasureId());
        assertNotNull(dto.getValueSetConversionResults());
        assertEquals(2, dto.getMeasureConversionResults().getMeasureResults().size());
        assertEquals(1, dto.getLibraryConversionResults().size());
        assertEquals(3, dto.getLibraryConversionResults().get(0).getLibraryResults().size());
    }

    private ConversionResult createConversionResult(boolean oidFound) {
        ConversionResult conversionResult = new ConversionResult();
        conversionResult.setMeasureId(MEASURE_ID);

        //conversionResult.setValueSetConversionResults(new ValueSetConversionResults());
        conversionResult.setMeasureConversionResults(new MeasureConversionResults());

        conversionResult.getValueSetConversionResults().addAll(createValueSetResults(oidFound));

        conversionResult.getMeasureConversionResults().setMeasureResults(createMeasureResults());

        conversionResult.getLibraryConversionResults().addAll(createLibraryResults());

        return conversionResult;
    }

    private List<ValueSetConversionResults> createValueSetResults(boolean oidFound) {
        ValueSetConversionResults valueSetConversionResults = new ValueSetConversionResults();
        valueSetConversionResults.setSuccess(oidFound);
        valueSetConversionResults.setReason("REASON");

        return Collections.singletonList(valueSetConversionResults);
    }

    private List<FieldConversionResult> createMeasureResults() {
        return Arrays.asList(buildMeasureResult(0), buildMeasureResult(1));
    }

    private List<LibraryConversionResults> createLibraryResults() {
        LibraryConversionResults libraryConversionResults = new LibraryConversionResults();
        List<FieldConversionResult> libraryResults =
                Arrays.asList(buildLibraryResult(0), buildLibraryResult(1), buildLibraryResult(2));
        libraryConversionResults.getLibraryResults().addAll(libraryResults);

        return Collections.singletonList(libraryConversionResults);
    }

    private FieldConversionResult buildLibraryResult(int i) {
        return FieldConversionResult.builder()
                .field("FIELD" + i)
                .destination("DESTINATION" + i)
                .reason("REASON" + i)
                .build();
    }

    private FieldConversionResult buildMeasureResult(int i) {
        return FieldConversionResult.builder()
                .field("FIELD" + i)
                .destination("DESTINATION" + i)
                .reason("REASON" + i)
                .build();
    }
}