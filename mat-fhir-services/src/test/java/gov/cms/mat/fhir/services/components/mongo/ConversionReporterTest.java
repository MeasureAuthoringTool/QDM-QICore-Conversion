package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.dto.FieldConversionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

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

        ConversionReporter.setInThreadLocal(MEASURE_ID, conversionResultsService, Instant.now());
        ConversionReporter.setMeasureResult(FIELD, DESTINATION, REASON);

        verify(conversionResultsService).addMeasureResult(any(), any(FieldConversionResult.class));
    }

    @Test
    void setLibraryResult_NoThreadLocal() {
        ConversionReporter.setLibraryFieldConversionResult(FIELD, DESTINATION, REASON, MAT_LIBRARY_ID);

        assertNull(ConversionReporter.getFromThreadLocal());

        verifyNoInteractions(conversionResultsService); // since no object in ThreadLocal no interactions
    }

    @Test
    void setLibraryResult_Success() {
        ConversionReporter.setInThreadLocal(MEASURE_ID, conversionResultsService, Instant.now());
        ConversionReporter.setLibraryFieldConversionResult(FIELD, DESTINATION, REASON, MAT_LIBRARY_ID);

        verify(conversionResultsService).addLibraryFieldConversionResult(any(), any(FieldConversionResult.class), anyString());
    }

    @Test
    void setValueSetResult_NoThreadLocal() {
        ConversionReporter.setValueSetInit(OID, REASON);

        verifyNoInteractions(conversionResultsService); // since no object in ThreadLocal no interactions
    }

//    @Test
//    void setValueSetResult_ThreadLocal() {
//        ConversionReporter.setInThreadLocal(MEASURE_ID, conversionResultsService);
//        ConversionReporter.setValueSetInit(OID, REASON);
//
//        verify(conversionResultsService).addValueSetResult(MEASURE_ID, OID, REASON, null, null);
//    }


}