package gov.cms.mat.fhir.services.components.reporting;


import gov.cms.mat.fhir.rest.dto.ConversionOutcome;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.rest.dto.CqlConversionError;
import gov.cms.mat.fhir.rest.dto.FhirValidationResult;
import gov.cms.mat.fhir.rest.dto.FieldConversionResult;
import gov.cms.mat.fhir.rest.dto.MatCqlConversionException;
import gov.cms.mat.fhir.rest.dto.MeasureConversionResults;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.LibraryConversionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ConversionResultsServiceTest {
    public static final String MAT_LIBRARY_ID = "matLibraryId";
    private static final String MEASURE_ID = "measureId";
    private ThreadSessionKey key;

    private ConversionResultsService conversionResultsService;

    @BeforeEach
    public void setUp() {
        LibraryDataService libraryDataService = new LibraryDataService();
        conversionResultsService = new ConversionResultsService(libraryDataService);

        key = ThreadSessionKey.builder()
                .measureId(MEASURE_ID)
                .start(Instant.now())
                .build();

        conversionResultsService.clear();
    }

    @Test
    void findByThreadSessionKeyNotFound() {
        Optional<ConversionResult> optional = conversionResultsService.findByThreadSessionKey(key);
        assertTrue(optional.isEmpty());
    }

    @Test
    void findConversionResultNotFound() {
        assertThrows(LibraryConversionException.class, () -> {
            conversionResultsService.findConversionResult(key);
        });
    }

    @Test
    void findConversionResultFound() {
        conversionResultsService.addMeasureResult(key, buildFieldConversionResult());
        assertNotNull(conversionResultsService.findConversionResult(key));
    }

    @Test
    void addMeasureResult() {
        FieldConversionResult fieldConversionResult = buildFieldConversionResult();
        conversionResultsService.addMeasureResult(key, fieldConversionResult);

        ConversionResult conversionResult = getConversionResult();

        assertEquals(1, conversionResult.getMeasureConversionResults().getMeasureResults().size());
        assertEquals(fieldConversionResult, conversionResult.getMeasureConversionResults().getMeasureResults().get(0));
    }

    @Test
    void addCqlConversionResultSuccess() {
        conversionResultsService.addCqlConversionResultSuccess(key, MAT_LIBRARY_ID);
        ConversionResult conversionResult = getConversionResult();
        assertEquals(1, conversionResult.getLibraryConversionResults().size());
        assertTrue(conversionResult.getLibraryConversionResults().get(0).getCqlConversionResult().getResult());
        assertEquals(MAT_LIBRARY_ID, conversionResult.getLibraryConversionResults().get(0).getMatLibraryId());
    }

    @Test
    void addCqlConversionErrorMessage() {
        conversionResultsService.addCqlConversionErrorMessage(key, "error", MAT_LIBRARY_ID);
        ConversionResult conversionResult = getConversionResult();
        assertEquals(1, conversionResult.getLibraryConversionResults().size());
        assertFalse(conversionResult.getLibraryConversionResults().get(0).getCqlConversionResult().getResult());
        assertEquals(MAT_LIBRARY_ID, conversionResult.getLibraryConversionResults().get(0).getMatLibraryId());
        assertEquals(1, conversionResult.getLibraryConversionResults().get(0).getCqlConversionResult().getErrors().size());
        assertEquals("error", conversionResult.getLibraryConversionResults().get(0).getCqlConversionResult().getErrors().get(0));
    }

    @Test
    void addCql() {
        conversionResultsService.addCql(key, "cql", "name", "version", MAT_LIBRARY_ID);
        ConversionResult conversionResult = getConversionResult();
        assertEquals(1, conversionResult.getLibraryConversionResults().size());
        assertEquals(MAT_LIBRARY_ID, conversionResult.getLibraryConversionResults().get(0).getMatLibraryId());
        assertEquals("name", conversionResult.getLibraryConversionResults().get(0).getName());
        assertEquals("version", conversionResult.getLibraryConversionResults().get(0).getVersion());

        assertEquals("cql", conversionResultsService.getCql(key, MAT_LIBRARY_ID));
    }


    @Test
    void addFhirCql() {
        conversionResultsService.addFhirCql(key, "fhir-cql", MAT_LIBRARY_ID);
        assertEquals("fhir-cql", conversionResultsService.getFhirCql(key, MAT_LIBRARY_ID));
    }

    @Test
    void addElm() {
        conversionResultsService.addElm(key, "elm", MAT_LIBRARY_ID);
        assertEquals("elm", conversionResultsService.getElm(key, MAT_LIBRARY_ID));
    }

    @Test
    void addFhirElmJson() {
        conversionResultsService.addFhirElmJson(key, "fhir-elm-json", MAT_LIBRARY_ID);
        assertEquals("fhir-elm-json", conversionResultsService.getFhirElmJson(key, MAT_LIBRARY_ID));
    }

    @Test
    void addFhirElmXml() {
        conversionResultsService.addFhirElmXml(key, "fhir-elm-xml", MAT_LIBRARY_ID);
        assertEquals("fhir-elm-xml", conversionResultsService.getFhirElmXml(key, MAT_LIBRARY_ID));
    }

    @Test
    void addFhirLibraryId() {
        conversionResultsService.addFhirLibraryId(key, "fhirLibraryId", MAT_LIBRARY_ID);
        ConversionResult conversionResult = getConversionResult();
        assertEquals(1, conversionResult.getLibraryConversionResults().size());
        assertEquals("fhirLibraryId", conversionResult.getLibraryConversionResults().get(0).getFhirLibraryId());

        Optional<String> optional = conversionResultsService.findFhirLibraryIdInMap(key, MAT_LIBRARY_ID);
        assertTrue(optional.isPresent());
        assertEquals("fhirLibraryId", optional.get());
    }

    @Test
    void addCqlConversionErrors() {
        List<CqlConversionError> errors = List.of(new CqlConversionError());
        conversionResultsService.addCqlConversionErrors(key, errors, MAT_LIBRARY_ID);

        ConversionResult conversionResult = getConversionResult();
        assertEquals(1, conversionResult.getLibraryConversionResults().size());
        assertFalse(conversionResult.getLibraryConversionResults().get(0).getCqlConversionResult().getResult());
        assertEquals(1, conversionResult.getLibraryConversionResults().get(0).getCqlConversionResult().getCqlConversionErrors().size());
        assertEquals(0, conversionResult.getLibraryConversionResults().get(0).getCqlConversionResult().getFhirCqlConversionErrors().size());
    }

    @Test
    void addFhirCqlConversionErrors() {
        List<CqlConversionError> errors = List.of(new CqlConversionError());
        conversionResultsService.addFhirCqlConversionErrors(key, errors, MAT_LIBRARY_ID);

        ConversionResult conversionResult = getConversionResult();
        assertEquals(1, conversionResult.getLibraryConversionResults().size());
        assertFalse(conversionResult.getLibraryConversionResults().get(0).getCqlConversionResult().getResult());
        assertEquals(0, conversionResult.getLibraryConversionResults().get(0).getCqlConversionResult().getCqlConversionErrors().size());
        assertEquals(1, conversionResult.getLibraryConversionResults().get(0).getCqlConversionResult().getFhirCqlConversionErrors().size());
    }

    @Test
    void addMatCqlConversionErrors() {
        List<MatCqlConversionException> errors = List.of(new MatCqlConversionException());
        conversionResultsService.addMatCqlConversionErrors(key, errors, MAT_LIBRARY_ID);

        ConversionResult conversionResult = getConversionResult();
        assertEquals(1, conversionResult.getLibraryConversionResults().size());
        assertFalse(conversionResult.getLibraryConversionResults().get(0).getCqlConversionResult().getResult());
        assertEquals(1, conversionResult.getLibraryConversionResults().get(0).getCqlConversionResult().getMatCqlConversionErrors().size());
        assertEquals(0, conversionResult.getLibraryConversionResults().get(0).getCqlConversionResult().getFhirMatCqlConversionErrors().size());
    }

    @Test
    void addFhirMatCqlConversionErrors() {
        List<MatCqlConversionException> errors = List.of(new MatCqlConversionException());
        conversionResultsService.addFhirMatCqlConversionErrors(key, errors, MAT_LIBRARY_ID);

        ConversionResult conversionResult = getConversionResult();
        assertEquals(1, conversionResult.getLibraryConversionResults().size());
        assertFalse(conversionResult.getLibraryConversionResults().get(0).getCqlConversionResult().getResult());
        assertEquals(0, conversionResult.getLibraryConversionResults().get(0).getCqlConversionResult().getMatCqlConversionErrors().size());
        assertEquals(1, conversionResult.getLibraryConversionResults().get(0).getCqlConversionResult().getFhirMatCqlConversionErrors().size());
    }

    @Test
    void addFhirMeasureValidationResults() {
        List<FhirValidationResult> validationResults = List.of(new FhirValidationResult());
        conversionResultsService.addFhirMeasureValidationResults(key, validationResults);
        ConversionResult conversionResult = getConversionResult();
        assertEquals(1, conversionResult.getMeasureConversionResults().getMeasureFhirValidationResults().size());
    }

    @Test
    void addLibraryValidationResults() {
        List<FhirValidationResult> validationResults = List.of(new FhirValidationResult());
        conversionResultsService.addLibraryValidationResults(key, validationResults, MAT_LIBRARY_ID);
        ConversionResult conversionResult = getConversionResult();
        assertEquals(1, conversionResult.getLibraryConversionResults().size());
        assertEquals(1, conversionResult.getLibraryConversionResults().get(0).getLibraryFhirValidationResults().size());
    }

    @Test
    void addLibraryConversionResult() {
        conversionResultsService.addLibraryConversionResult(key, "link", "message", Boolean.TRUE, MAT_LIBRARY_ID);
        ConversionResult conversionResult = getConversionResult();
        assertEquals(1, conversionResult.getLibraryConversionResults().size());
        assertEquals("link", conversionResult.getLibraryConversionResults().get(0).getLink());
        assertEquals("message", conversionResult.getLibraryConversionResults().get(0).getReason());
        assertTrue(conversionResult.getLibraryConversionResults().get(0).getSuccess());
        assertEquals(MAT_LIBRARY_ID, conversionResult.getLibraryConversionResults().get(0).getMatLibraryId());
    }

    @Test
    void addErrorMessage() {
        conversionResultsService.addErrorMessage(key, "message", ConversionOutcome.INTERNAL_SERVER_ERROR);
        assertEquals("message", getConversionResult().getErrorReason());
        assertEquals(ConversionOutcome.INTERNAL_SERVER_ERROR, getConversionResult().getOutcome());
    }

    @Test
    void addValueSetProcessingMemo() {
        conversionResultsService.addValueSetProcessingMemo(key, "memo");
        assertEquals("memo", getConversionResult().getValueSetProcessingMemo());
    }

    @Test
    void addFhirMeasureJson() {
        conversionResultsService.addFhirMeasureJson(key, "fhir-measure-json");
        assertEquals("fhir-measure-json", getConversionResult().getMeasureConversionResults().getFhirMeasureJson());
    }

    @Test
    void addMeasureConversionResult() {
        conversionResultsService.addMeasureConversionResult(key, "link", "reason", Boolean.TRUE);
        MeasureConversionResults measureConversionResults = getConversionResult().getMeasureConversionResults();
        assertEquals("link", measureConversionResults.getLink());
        assertEquals("reason", measureConversionResults.getReason());
        assertTrue(measureConversionResults.getSuccess());
    }

    @Test
    void complete() {
        conversionResultsService.complete(key);
        assertNotNull(getConversionResult().getFinished());
    }

    @Test
    void addConversionType() {
        conversionResultsService.addConversionType(key, ConversionType.CONVERSION);
        assertEquals(ConversionType.CONVERSION, getConversionResult().getConversionType());
    }

    @Test
    void addBatchId() {
        conversionResultsService.addBatchId(key, "batchId");
        assertEquals("batchId", getConversionResult().getBatchId());
    }

    @Test
    void addXmlSource() {
        conversionResultsService.addXmlSource(key, XmlSource.SIMPLE);
        assertEquals(XmlSource.SIMPLE, getConversionResult().getXmlSource());
    }

    @Test
    void addMeasureLibraryId() {
        conversionResultsService.addMeasureLibraryId(key, "id");
        assertEquals("id", getConversionResult().getFhirMeasureId());
    }

    @Test
    void addShowWarnings() {
        conversionResultsService.addShowWarnings(key, Boolean.FALSE);
        assertFalse(getConversionResult().getShowWarnings());
    }

    @Test
    void addVsacGrantingTicket() {
        conversionResultsService.addVsacGrantingTicket(key, "vsacGrantingTicket");
        assertEquals("vsacGrantingTicket", getConversionResult().getVsacGrantingTicket());
    }


    private ConversionResult getConversionResult() {
        Optional<ConversionResult> optional = conversionResultsService.findByThreadSessionKey(key);
        ConversionResult conversionResult = optional.orElseThrow(IllegalArgumentException::new);
        assertEquals(MEASURE_ID, conversionResult.getSourceMeasureId());
        return conversionResult;
    }

    private FieldConversionResult buildFieldConversionResult() {
        return FieldConversionResult.builder()
                .field("FIELD")
                .destination("DESTINATION")
                .reason("REASON")
                .build();
    }

}