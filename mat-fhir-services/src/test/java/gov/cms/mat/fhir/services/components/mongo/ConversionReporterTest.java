package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.cql.ConversionType;
import gov.cms.mat.fhir.rest.cql.FieldConversionResult;
import gov.cms.mat.fhir.rest.cql.ValueSetResult;
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
    private static final String DESTINATION = "destination";
    private static final String REASON = "reason";

    ConversionResultsService conversionResultsService;

    @BeforeEach
    void setUp() {
        conversionResultsService = mock(ConversionResultsService.class);
        ConversionReporter.removeInThreadLocal();
    }

    @Test
    void setMeasureResult_NoThreadLocal() {
        ConversionReporter.setMeasureResult(FIELD, DESTINATION, REASON);

        assertNull(ConversionReporter.getFromThreadLocal());

        verifyNoInteractions(conversionResultsService); // since no object in ThreadLocal no interactions
    }

    @Test
    void setMeasureResult_Success() {
        when(conversionResultsService.addMeasureResult(anyString(), any(FieldConversionResult.class)))
                .thenReturn(new ConversionResult());

        ConversionReporter.setInThreadLocal(MEASURE_ID, conversionResultsService);
        ConversionReporter.resetMeasure(ConversionType.VALIDATION);
        ConversionReporter.setMeasureResult(FIELD, DESTINATION, REASON);

        verify(conversionResultsService).addMeasureResult(anyString(), any(FieldConversionResult.class));
    }

    @Test
    void setLibraryResult_NoThreadLocal() {
        ConversionReporter.setLibraryResult(FIELD, DESTINATION, REASON);

        assertNull(ConversionReporter.getFromThreadLocal());

        verifyNoInteractions(conversionResultsService); // since no object in ThreadLocal no interactions
    }

    @Test
    void setLibraryResult_Success() {
        when(conversionResultsService.addLibraryResult(anyString(), any(FieldConversionResult.class)))
                .thenReturn(new ConversionResult());

        ConversionReporter.setInThreadLocal(MEASURE_ID, conversionResultsService);
        ConversionReporter.resetLibrary(ConversionType.VALIDATION);
        ConversionReporter.setLibraryResult(FIELD, DESTINATION, REASON);

        verify(conversionResultsService).addLibraryResult(anyString(), any(FieldConversionResult.class));
    }

    @Test
    void setValueSetResult_NoThreadLocal() {
        ConversionReporter.setValueSetFailResult("OID", REASON);

        verifyNoInteractions(conversionResultsService); // since no object in ThreadLocal no interactions
    }

    @Test
    void setValueSetResult_ThreadLocal() {
        ConversionReporter.setInThreadLocal(MEASURE_ID, conversionResultsService);
        ConversionReporter.setValueSetFailResult("OID", REASON);

        verify(conversionResultsService).addValueSetResult(anyString(), any(ValueSetResult.class));
    }

    @Test
    void resetValueSetResults_NoThreadLocal() {
        Assertions.assertThrows(ThreadLocalNotFoundException.class, () -> ConversionReporter.resetValueSetResults(ConversionType.VALIDATION));
        verifyNoInteractions(conversionResultsService); // since no object in ThreadLocal no interactions
    }

    @Test
    void resetMeasure_NoThreadLocal() {
        Assertions.assertThrows(ThreadLocalNotFoundException.class, () -> ConversionReporter.resetMeasure(ConversionType.VALIDATION));
        verifyNoInteractions(conversionResultsService); // since no object in ThreadLocal no interactions
    }

    @Test
    void resetLibrary_NoThreadLocal() {
        Assertions.assertThrows(ThreadLocalNotFoundException.class, () -> ConversionReporter.resetLibrary(null));
        verifyNoInteractions(conversionResultsService); // since no object in ThreadLocal no interactions
    }
}