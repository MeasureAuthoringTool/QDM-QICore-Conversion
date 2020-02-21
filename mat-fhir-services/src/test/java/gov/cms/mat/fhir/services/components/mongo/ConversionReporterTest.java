package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.rest.dto.FieldConversionResult;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
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

        ConversionReporter.setInThreadLocal(MEASURE_ID,
                "TEST",
                conversionResultsService,
                Instant.now(),
                ConversionType.CONVERSION,
                XmlSource.SIMPLE,
                Boolean.TRUE);
        ConversionReporter.setMeasureResult(FIELD, DESTINATION, REASON);

        verify(conversionResultsService).addMeasureResult(any(), any(FieldConversionResult.class));
    }



    @Test
    void setValueSetResult_NoThreadLocal() {
        ConversionReporter.setValueSetInit(OID, REASON, null);

        verifyNoInteractions(conversionResultsService); // since no object in ThreadLocal no interactions
    }
}