package gov.cms.mat.fhir.services.components.reporting;

import gov.cms.mat.fhir.rest.dto.*;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.ConversionResultsNotFoundException;
import gov.cms.mat.fhir.services.service.MappingDataService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversionResultProcessorServiceTest {
    private static final String MEASURE_ID = "measure_id";

    private ThreadSessionKey threadSessionKey;

    @Mock
    private MappingDataService mappingDataService;
    @Mock
    private ConversionResultsService conversionResultsService;

    @InjectMocks
    private ConversionResultProcessorService conversionResultProcessorService;

    @BeforeEach
    public void setUp() {
        ConversionResultsService conversionResultsService = mock(ConversionResultsService.class);

        threadSessionKey = ConversionReporter.setInThreadLocal(MEASURE_ID,
                "2",
                conversionResultsService,
                Instant.now(),
                ConversionType.CONVERSION,
                XmlSource.SIMPLE,
                true,
                null);
    }


    @Test
    void processSearchData_NotFound() {
        when(conversionResultsService.findByThreadSessionKey(threadSessionKey)).thenReturn(Optional.empty());

        ConversionResultsNotFoundException thrown =
                Assertions.assertThrows(ConversionResultsNotFoundException.class, () -> {
                    conversionResultProcessorService.process(threadSessionKey);
                });

        assertTrue(thrown.getMessage().contains(MEASURE_ID));
        verify(conversionResultsService).findByThreadSessionKey(threadSessionKey);
    }

    @Test
    void processSearchData_HappyPath() {
        when(conversionResultsService.findByThreadSessionKey(threadSessionKey))
                .thenReturn(Optional.of(createConversionResult(true)));

        ConversionResultDto dto = conversionResultProcessorService.process(threadSessionKey);
        verifyResult(dto);
        verify(conversionResultsService).findByThreadSessionKey(threadSessionKey);

    }


    private ConversionResultDto verifyResults(List<ConversionResultDto> conversionResults) {
        assertEquals(1, conversionResults.size());

        ConversionResultDto dto = conversionResults.get(0);
        verifyResult(dto);

        return dto;
    }

    private void verifyResult(ConversionResultDto dto) {
        assertEquals(MEASURE_ID, dto.getMeasureId());
        assertEquals(2, dto.getMeasureConversionResults().getMeasureResults().size());
        assertEquals(1, dto.getLibraryConversionResults().size());
    }

    private ConversionResult createConversionResult(boolean oidFound) {
        ConversionResult conversionResult = new ConversionResult();
        conversionResult.setSourceMeasureId(MEASURE_ID);

        //conversionResult.setValueSetConversionResults(new ValueSetConversionResults());
        conversionResult.setMeasureConversionResults(new MeasureConversionResults());


        conversionResult.getMeasureConversionResults().setMeasureResults(createMeasureResults());

        conversionResult.getLibraryConversionResults().addAll(createLibraryResults());

        return conversionResult;
    }

    private List<FieldConversionResult> createMeasureResults() {
        return Arrays.asList(buildMeasureResult(0), buildMeasureResult(1));
    }

    private List<LibraryConversionResults> createLibraryResults() {
        LibraryConversionResults libraryConversionResults = new LibraryConversionResults();
        List<FieldConversionResult> libraryResults =
                Arrays.asList(buildLibraryResult(0), buildLibraryResult(1), buildLibraryResult(2));
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