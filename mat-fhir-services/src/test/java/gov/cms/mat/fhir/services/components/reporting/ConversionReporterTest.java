package gov.cms.mat.fhir.services.components.reporting;

import gov.cms.mat.fhir.rest.dto.ConversionOutcome;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.rest.dto.CqlConversionError;
import gov.cms.mat.fhir.rest.dto.FhirValidationResult;
import gov.cms.mat.fhir.rest.dto.FieldConversionResult;
import gov.cms.mat.fhir.rest.dto.MatCqlConversionException;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversionReporterTest {
    private static final String OID = "oid";
    private static final String MEASURE_ID = "measureId";
    private static final String FIELD = "field";
    private static final String DESTINATION = "destination";
    private static final String REASON = "reason";
    private static final String MAT_LIBRARY_ID = "matLibraryId";
    private static final String FHIR_LIBRARY_ID = "fhirLibraryId";

    @Mock
    private ConversionResultsService conversionResultsService;


    @BeforeEach
    void setUp() {
        ConversionReporter.removeInThreadLocal();
    }

    @Test
    void setCqlConversionResultSuccess() {
        ThreadSessionKey key = buildThreadKey();
        ConversionReporter.setCqlConversionResultSuccess(MAT_LIBRARY_ID);
        verify(conversionResultsService).addCqlConversionResultSuccess(key, MAT_LIBRARY_ID);
    }

    @Test
    void setCqlConversionErrorMessage() {
        ThreadSessionKey key = buildThreadKey();
        ConversionReporter.setCqlConversionErrorMessage("ERROR", MAT_LIBRARY_ID);
        verify(conversionResultsService).addCqlConversionErrorMessage(key, "ERROR", MAT_LIBRARY_ID);
    }

    @Test
    void setFhirCql() {
        ThreadSessionKey key = buildThreadKey();
        String cql = "CQL";
        ConversionReporter.setFhirCql(cql, MAT_LIBRARY_ID);

        verify(conversionResultsService).addFhirCql(key, cql, MAT_LIBRARY_ID);
    }

    @Test
    void setCqlConversionErrors() {
        ThreadSessionKey key = buildThreadKey();
        List<CqlConversionError> errors = List.of(new CqlConversionError());
        ConversionReporter.setCqlConversionErrors(errors, MAT_LIBRARY_ID);

        verify(conversionResultsService).addCqlConversionErrors(key, errors, MAT_LIBRARY_ID);
    }

    @Test
    void setFhirCqlConversionErrors() {
        ThreadSessionKey key = buildThreadKey();
        List<CqlConversionError> errors = List.of(new CqlConversionError());
        ConversionReporter.setFhirCqlConversionErrors(errors, MAT_LIBRARY_ID);

        verify(conversionResultsService).addFhirCqlConversionErrors(key, errors, MAT_LIBRARY_ID);
    }

    @Test
    void setMatCqlConversionExceptions() {
        ThreadSessionKey key = buildThreadKey();

        List<MatCqlConversionException> exceptions = List.of(new MatCqlConversionException());
        ConversionReporter.setMatCqlConversionExceptions(exceptions, MAT_LIBRARY_ID);

        verify(conversionResultsService).addMatCqlConversionErrors(key, exceptions, MAT_LIBRARY_ID);
    }

    @Test
    void setFhirMatCqlConversionExceptions() {
        ThreadSessionKey key = buildThreadKey();

        List<MatCqlConversionException> exceptions = List.of(new MatCqlConversionException());
        ConversionReporter.setFhirMatCqlConversionExceptions(exceptions, MAT_LIBRARY_ID);

        verify(conversionResultsService).addFhirMatCqlConversionErrors(key, exceptions, MAT_LIBRARY_ID);
    }

    @Test
    void setCqlNullVersion() {
        ThreadSessionKey key = buildThreadKey();
        ConversionReporter.setCql("cql", "name", null, MAT_LIBRARY_ID);

        verify(conversionResultsService).addCql(key, "cql", "name", "null", MAT_LIBRARY_ID);
    }

    @Test
    void setCql() {
        BigDecimal bigDecimal = new BigDecimal("1.1");
        ThreadSessionKey key = buildThreadKey();
        ConversionReporter.setCql("cql", "name", bigDecimal, MAT_LIBRARY_ID);

        verify(conversionResultsService).addCql(key, "cql", "name", "1.1", MAT_LIBRARY_ID);
    }

    @Test
    void setExternalLibraryErrors() {
        ThreadSessionKey key = buildThreadKey();
        List<CqlConversionError> errors = List.of(new CqlConversionError());
        Map<String, List<CqlConversionError>> map = new HashMap<>();
        map.put("key", errors);

        ConversionReporter.setExternalLibraryErrors(map, MAT_LIBRARY_ID);

        verify(conversionResultsService).addExternalLibraryErrors(key, map, MAT_LIBRARY_ID);
    }


    @Test
    void setFhirLibraryId() {
        ThreadSessionKey key = buildThreadKey();
        ConversionReporter.setFhirLibraryId(FHIR_LIBRARY_ID, MAT_LIBRARY_ID);

        verify(conversionResultsService).addFhirLibraryId(key, FHIR_LIBRARY_ID, MAT_LIBRARY_ID);
    }

    @Test
    void findFhirLibraryId() {
        ThreadSessionKey key = buildThreadKey();
        when(conversionResultsService.findFhirLibraryIdInMap(key, MAT_LIBRARY_ID))
                .thenReturn(Optional.of(FHIR_LIBRARY_ID));

        Optional<String> optional = ConversionReporter.findFhirLibraryId(MAT_LIBRARY_ID);
        assertTrue(optional.isPresent());
        assertEquals(FHIR_LIBRARY_ID, optional.get());
    }

    @Test
    void getFhirElmXml() {
        ThreadSessionKey key = buildThreadKey();
        when(conversionResultsService.getFhirElmXml(key, MAT_LIBRARY_ID)).thenReturn("FHIR_ELM_XML");

        String fhirElmXml = ConversionReporter.getFhirElmXml(MAT_LIBRARY_ID);
        assertEquals("FHIR_ELM_XML", fhirElmXml);
    }

    @Test
    void setElmJson() {
        ThreadSessionKey key = buildThreadKey();
        ConversionReporter.setElmJson("json", MAT_LIBRARY_ID);
        verify(conversionResultsService).addElm(key, "json", MAT_LIBRARY_ID);
    }

    @Test
    void setValueSetInit() {
        ThreadSessionKey key = buildThreadKey();
        ConversionReporter.setValueSetInit(OID, "reason", Boolean.TRUE);
        verify(conversionResultsService).addValueSetResult(key, OID, "reason", Boolean.TRUE, null);
    }

    @Test
    void setValueSetJson() {
        ThreadSessionKey key = buildThreadKey();

        ConversionReporter.setValueSetJson(OID, "json");

        verify(conversionResultsService).addValueSetJson(key, OID, "json");
    }

    @Test
    void setFhirLibraryValidationResults() {
        ThreadSessionKey key = buildThreadKey();
        List<FhirValidationResult> list = List.of(new FhirValidationResult());
        ConversionReporter.setFhirLibraryValidationResults(list, MAT_LIBRARY_ID);
        verify(conversionResultsService).addLibraryValidationResults(key, list, MAT_LIBRARY_ID);
    }

    @Test
    void setMeasureResult_NoThreadLocal() {
        ConversionReporter.setMeasureResult(FIELD, DESTINATION, REASON);

        assertNull(ConversionReporter.getFromThreadLocal());

        verifyNoInteractions(conversionResultsService); // since no object in ThreadLocal no interactions
    }

    @Test
    void setLibraryValidationLink() {
        ThreadSessionKey key = buildThreadKey();

        ConversionReporter.setLibraryValidationLink("link",
                HapiResourcePersistedState.CREATED,
                MAT_LIBRARY_ID);

        verify(conversionResultsService).addLibraryConversionResult(key,
                "link",
                HapiResourcePersistedState.CREATED.value,
                Boolean.TRUE,
                MAT_LIBRARY_ID);
    }

    @Test
    void setLibraryValidationError() {
        ThreadSessionKey key = buildThreadKey();

        ConversionReporter.setLibraryValidationError("link", "reason", MAT_LIBRARY_ID);

        verify(conversionResultsService).addLibraryConversionResult(key,
                "link",
                "reason",
                Boolean.FALSE
                , MAT_LIBRARY_ID);
    }

    @Test
    void setTerminalMessage() {
        ThreadSessionKey key = buildThreadKey();

        ConversionReporter.setTerminalMessage("errorMessage", ConversionOutcome.INTERNAL_SERVER_ERROR);

        verify(conversionResultsService).addErrorMessage(key, "errorMessage", ConversionOutcome.INTERNAL_SERVER_ERROR);
    }

    @Test
    void setTerminalMessageError() {
        ThreadSessionKey key = buildThreadKey();

        doThrow(new IllegalArgumentException("oops")).when(conversionResultsService).addErrorMessage(key, "errorMessage", ConversionOutcome.INTERNAL_SERVER_ERROR);

        ConversionReporter.setTerminalMessage("errorMessage", ConversionOutcome.INTERNAL_SERVER_ERROR);
        verify(conversionResultsService).addErrorMessage(key, "errorMessage", ConversionOutcome.INTERNAL_SERVER_ERROR);
    }

    @Test
    void setMeasureResult_Success() {

        buildThreadKey();

        ConversionReporter.setMeasureResult(FIELD, DESTINATION, REASON);

        verify(conversionResultsService).addMeasureResult(any(), any(FieldConversionResult.class));
    }

    @Test
    void setValueSetResult_NoThreadLocal() {
        ConversionReporter.setValueSetInit(OID, REASON, null);

        verifyNoInteractions(conversionResultsService); // since no object in ThreadLocal no interactions
    }

    private ThreadSessionKey buildThreadKey() {
        return ConversionReporter.setInThreadLocal(MEASURE_ID,
                "TEST",
                conversionResultsService,
                Instant.now(),
                ConversionType.CONVERSION,
                XmlSource.SIMPLE,
                Boolean.TRUE,
                null);
    }

}