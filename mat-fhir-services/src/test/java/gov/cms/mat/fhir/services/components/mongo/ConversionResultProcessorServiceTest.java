package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.services.exceptions.ConversionResultsNotFound;
import gov.cms.mat.fhir.services.exceptions.QdmQiCoreDataException;
import gov.cms.mat.fhir.services.service.QdmQiCoreDataService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversionResultProcessorServiceTest {

    private static final String MEASURE_ID = "measure_id";

    @Mock
    private QdmQiCoreDataService qdmQiCoreDataService;
    @Mock
    private ConversionResultsService conversionResultsService;

    @InjectMocks
    private ConversionResultProcessorService conversionResultProcessorService;

    @Test
    void processAll_HappyPath() {
        when(conversionResultsService.findAll())
                .thenReturn(Collections.singletonList(createConversionResult()));

        List<ConversionResultDto> conversionResults = conversionResultProcessorService.processAll();
        ConversionResultDto dto = verifyResults(conversionResults);

        dto.getMeasureResults().forEach(r -> assertNull(r.getErrorMessage()));
        dto.getMeasureResults().forEach(r -> assertNull(r.getConversionMapping()));

        verify(conversionResultsService).findAll();
        verify(qdmQiCoreDataService, times(2))
                .findByFhirR4QiCoreMapping(anyString(), anyString());
    }


    @Test
    void processAll_DataServiceError() {
        when(conversionResultsService.findAll())
                .thenReturn(Collections.singletonList(createConversionResult()));

        String errorMessage = "The core is damaged";

        when(qdmQiCoreDataService.findByFhirR4QiCoreMapping(anyString(), anyString()))
                .thenThrow(new QdmQiCoreDataException(errorMessage));

        List<ConversionResultDto> conversionResults = conversionResultProcessorService.processAll();
        ConversionResultDto dto = verifyResults(conversionResults);

        dto.getMeasureResults().forEach(r -> assertEquals(errorMessage, r.getErrorMessage()));
        dto.getMeasureResults().forEach(r -> assertNull(r.getConversionMapping()));

        verify(conversionResultsService).findAll();
        verify(qdmQiCoreDataService, times(2))
                .findByFhirR4QiCoreMapping(anyString(), anyString());
    }

    @Test
    void processSearchData_NotFound() {
        when(conversionResultsService.findByMeasureId(MEASURE_ID)).thenReturn(Optional.empty());

        ConversionResultsNotFound thrown =
                Assertions.assertThrows(ConversionResultsNotFound.class, () -> {
                    conversionResultProcessorService.process(MEASURE_ID);
                });

        assertTrue(thrown.getMessage().contains(MEASURE_ID));
        verify(conversionResultsService).findByMeasureId(MEASURE_ID);
    }

    @Test
    void processSearchData_HappyPath() {
        when(conversionResultsService.findByMeasureId(MEASURE_ID)).thenReturn(Optional.of(createConversionResult()));

        ConversionResultDto dto = conversionResultProcessorService.process(MEASURE_ID);
        verifyResult(dto);
        dto.getMeasureResults().forEach(r -> assertNull(r.getErrorMessage()));

        verify(conversionResultsService).findByMeasureId(MEASURE_ID);
        verify(qdmQiCoreDataService, times(2))
                .findByFhirR4QiCoreMapping(anyString(), anyString());
    }


    private ConversionResultDto verifyResults(List<ConversionResultDto> conversionResults) {
        assertEquals(1, conversionResults.size());

        ConversionResultDto dto = conversionResults.get(0);
        verifyResult(dto);

        return dto;
    }

    private void verifyResult(ConversionResultDto dto) {
        assertEquals(MEASURE_ID, dto.getMeasureId());
        assertEquals(1, dto.getValueSetResults().size());
        assertEquals(2, dto.getMeasureResults().size());
        assertEquals(3, dto.getLibraryResults().size());
    }

    private ConversionResult createConversionResult() {
        ConversionResult conversionResult = new ConversionResult();
        conversionResult.setMeasureId(MEASURE_ID);

        conversionResult.setValueSetResults(createValueSetResults());
        conversionResult.setMeasureResults(createMeasureResults());
        conversionResult.setLibraryResults(createLibraryResults());

        return conversionResult;
    }

    private List<ConversionResult.ValueSetResult> createValueSetResults() {
        return Collections.singletonList(ConversionResult.ValueSetResult
                .builder()
                .oid("OID")
                .reason("REASON")
                .build());
    }

    private List<ConversionResult.MeasureResult> createMeasureResults() {
        return Arrays.asList(buildMeasureResult(0), buildMeasureResult(1));
    }

    private List<ConversionResult.LibraryResult> createLibraryResults() {
        return Arrays.asList(buildLibraryResult(0), buildLibraryResult(1), buildLibraryResult(2));
    }

    private ConversionResult.LibraryResult buildLibraryResult(int i) {
        return ConversionResult.LibraryResult.builder()
                .field("FIELD" + i)
                .destination("DESTINATION" + i)
                .reason("REASON" + i)
                .build();
    }

    private ConversionResult.MeasureResult buildMeasureResult(int i) {
        return ConversionResult.MeasureResult.builder()
                .field("FIELD" + i)
                .destination("DESTINATION" + i)
                .reason("REASON" + i)
                .build();
    }
}