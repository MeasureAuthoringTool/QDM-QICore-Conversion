package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.dto.FieldConversionResult;
import gov.cms.mat.fhir.services.exceptions.ThreadLocalNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversionReporterTest {
    private static final String OID = "oid";
    private static final String MEASURE_ID = "measureId";
    private static final String FIELD = "field";
    private static final String DESTINATION = "destination";
    private static final String REASON = "reason";
    private static final String MAT_LIBRARY_ID = "matLibraryId";
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

        ConversionReporter.setInThreadLocal(MEASURE_ID, conversionResultsService);
        ConversionReporter.resetMeasure();
        ConversionReporter.setMeasureResult(FIELD, DESTINATION, REASON);

        verify(conversionResultsService).addMeasureResult(anyString(), any(FieldConversionResult.class));
    }

    @Test
    void setLibraryResult_NoThreadLocal() {
        ConversionReporter.setLibraryFieldConversionResult(FIELD, DESTINATION, REASON, MAT_LIBRARY_ID);

        assertNull(ConversionReporter.getFromThreadLocal());

        verifyNoInteractions(conversionResultsService); // since no object in ThreadLocal no interactions
    }

    @Test
    void setLibraryResult_Success() {
        ConversionReporter.setInThreadLocal(MEASURE_ID, conversionResultsService);
        ConversionReporter.resetLibrary();
        ConversionReporter.setLibraryFieldConversionResult(FIELD, DESTINATION, REASON, MAT_LIBRARY_ID);

        verify(conversionResultsService).addLibraryFieldConversionResult(anyString(), any(FieldConversionResult.class), anyString());
    }

    @Test
    void setValueSetResult_NoThreadLocal() {
        ConversionReporter.setValueSetInit(OID, REASON);

        verifyNoInteractions(conversionResultsService); // since no object in ThreadLocal no interactions
    }

    @Test
    void setValueSetResult_ThreadLocal() {
        ConversionReporter.setInThreadLocal(MEASURE_ID, conversionResultsService);
        ConversionReporter.setValueSetInit(OID, REASON);

        verify(conversionResultsService).addValueSetResult(MEASURE_ID, OID, REASON, null, null);
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

    @Test
    void resetLibrary_NoThreadLocal() {
        Assertions.assertThrows(ThreadLocalNotFoundException.class, ConversionReporter::resetLibrary);
        verifyNoInteractions(conversionResultsService); // since no object in ThreadLocal no interactions
    }
}