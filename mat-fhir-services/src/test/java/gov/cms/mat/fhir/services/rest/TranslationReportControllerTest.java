package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.rest.dto.ConversionResultDto;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultProcessorService;
import gov.cms.mat.fhir.services.components.mongo.ThreadSessionKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TranslationReportControllerTest {
    private static final String MEASURE_ID = "measure_id";
    private static final String BATCH_ID = "measure_id";

    private ThreadSessionKey threadSessionKey;

    @Mock
    private ConversionResultProcessorService conversionResultProcessorService;

    @InjectMocks
    private TranslationReportController translationReportController;

    @BeforeEach
    public void setUp() {
        threadSessionKey = ThreadSessionKey.builder()
                .measureId(MEASURE_ID)
                .start(Instant.now())
                .build();
    }

    @Test
    void findMissingValueSets() {
        when(conversionResultProcessorService.findMissingValueSets(BATCH_ID)).thenReturn(new HashSet<>(Arrays.asList("1", "2", "3")));

        assertEquals(3, translationReportController.findMissingValueSets(BATCH_ID).size());

        verify(conversionResultProcessorService).findMissingValueSets(BATCH_ID);

    }


    @Test
    void findAll() {
        ConversionResultDto conversionResultToReturn = ConversionResultDto.builder().build();
        when(conversionResultProcessorService.processAllForBatch(BATCH_ID)).thenReturn(Collections.singletonList(conversionResultToReturn));

        List<ConversionResultDto> conversionResults = translationReportController.findAll(BATCH_ID);
        assertEquals(1, conversionResults.size());
        assertEquals(conversionResultToReturn, conversionResults.get(0));

        verify(conversionResultProcessorService).processAllForBatch(BATCH_ID);
    }
}