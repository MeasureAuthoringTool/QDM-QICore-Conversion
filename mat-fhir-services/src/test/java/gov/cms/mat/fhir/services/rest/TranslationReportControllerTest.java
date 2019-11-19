package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.services.components.mongo.ConversionResultDto;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultProcessorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TranslationReportControllerTest {
    @Mock
    private ConversionResultProcessorService conversionResultProcessorService;

    @InjectMocks
    private TranslationReportController translationReportController;

    @Test
    void findMissingValueSets() {
        when(conversionResultProcessorService.findMissingValueSets()).thenReturn(new HashSet<>(Arrays.asList("1", "2", "3")));

        assertEquals(3, translationReportController.findMissingValueSets().size());

        verify(conversionResultProcessorService).findMissingValueSets();

    }

    @Test
    void findSearchData() {
        String measureId = "MEASURE_ID";

        ConversionResultDto conversionResultToReturn = ConversionResultDto.builder().build();
        when(conversionResultProcessorService.process(measureId)).thenReturn(conversionResultToReturn);

        ConversionResultDto conversionResultReturned = translationReportController.findSearchData(measureId);
        assertEquals(conversionResultToReturn, conversionResultReturned);

        verify(conversionResultProcessorService).process(measureId);
    }

    @Test
    void findAll() {
        ConversionResultDto conversionResultToReturn = ConversionResultDto.builder().build();
        when(conversionResultProcessorService.processAll()).thenReturn(Collections.singletonList(conversionResultToReturn));

        List<ConversionResultDto> conversionResults = translationReportController.findAll();
        assertEquals(1, conversionResults.size());
        assertEquals(conversionResultToReturn, conversionResults.get(0));

        verify(conversionResultProcessorService).processAll();

    }
}