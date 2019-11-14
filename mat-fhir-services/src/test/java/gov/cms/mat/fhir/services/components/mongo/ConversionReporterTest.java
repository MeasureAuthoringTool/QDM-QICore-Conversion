package gov.cms.mat.fhir.services.components.mongo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversionReporterTest {
    private static final String MEASURE_ID = "measureId";
    private static final String FIELD = "field";
    private static final String REASON = "reason";

    ConversionResultsService conversionResultsService;

    @BeforeEach
    void setUp() {
        conversionResultsService = mock(ConversionResultsService.class);
        ConversionReporter.removeInThreadLocal();
    }

    @Test
    void setMeasureResult_NoThreadLocal() {
        ConversionReporter.setMeasureResult(FIELD, REASON);

        assertNull(ConversionReporter.getFromThreadLocal());

        verifyNoInteractions(conversionResultsService); // since no object in ThreadLocal no interactions
    }

    @Test
    void setMeasureResult_Success() {
        when(conversionResultsService.addMeasureResult(anyString(), any(ConversionResult.MeasureResult.class)))
                .thenReturn(new ConversionResult());

        ConversionReporter.setInThreadLocal(MEASURE_ID, conversionResultsService);
        ConversionReporter.resetMeasure();
        ConversionReporter.setMeasureResult(FIELD, REASON);

        verify(conversionResultsService).addMeasureResult(anyString(), any(ConversionResult.MeasureResult.class));
    }

    @Test
    void resetValueSetResults_NoThreadLocal() {
        Assertions.assertThrows(ThreadLocalNotFoundException.class, ConversionReporter::resetValueSetResults);
        verifyNoInteractions(conversionResultsService); // since no object in ThreadLocal no interactions
    }

    @Test
    void resetMeasure_NoThreadLocal() {
        Assertions.assertThrows(ThreadLocalNotFoundException.class, ConversionReporter::resetMeasure);
        verifyNoInteractions(conversionResultsService); // since no object in ThreadLocal no interactions
    }
}