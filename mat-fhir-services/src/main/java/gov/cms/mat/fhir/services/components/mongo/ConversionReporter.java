package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.dto.*;
import gov.cms.mat.fhir.services.exceptions.ThreadLocalNotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;

@Slf4j
public class ConversionReporter {
    private static final String NOT_FOUND_THREAD_LOCAL_MESSAGE = "Cannot get (null)conversionReporter from threadLocal";

    private static final ThreadLocal<ConversionReporter> threadLocal = new ThreadLocal<>();

    private final ConversionKey key;

    private final String measureId;
    private final Instant start;
    private final ConversionResultsService conversionResultsService;

    private ConversionReporter(String measureId, ConversionResultsService conversionResultsService) {
        this.measureId = measureId;
        this.conversionResultsService = conversionResultsService;
        start = Instant.now();

        key = ConversionKey.builder().measureId(measureId).start(Instant.now()).build();
    }

    public static void saveConversionResult(ConversionResult conversionResult) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.saveConversionResultToMongo(conversionResult);
    }


    public static void setMeasureResult(String field, String destination, String reason) {
        ConversionReporter conversionReporter = getFromThreadLocal();

        if (conversionReporter != null) {
            conversionReporter.addMeasureResult(field, destination, reason);
        }
    }

    public static void setFhirMeasureJson(String json) {
        ConversionReporter conversionReporter = getFromThreadLocal();

        if (conversionReporter != null) {
            conversionReporter.addFhirMeasureJson(json);
        }
    }

    public static void setLibraryFieldConversionResult(String field, String destination, String reason, String matLibraryId) {
        ConversionReporter conversionReporter = getFromThreadLocal();

        if (conversionReporter != null) {
            conversionReporter.addLibraryFieldConversionResult(field, destination, reason, matLibraryId);
        }
    }

    public static void setCqlConversionResultSuccess(String matLibraryId) {
        ConversionReporter conversionReporter = getConversionReporter();

        conversionReporter.addCqlConversionResultSuccess(matLibraryId);
    }

    public static void setCqlConversionErrorMessage(String error, String matLibraryId) {
        ConversionReporter conversionReporter = getConversionReporter();

        conversionReporter.addCqlConversionErrorMessage(error, matLibraryId);
    }

    public static void setCqlConversionErrors(List<CqlConversionError> errors, String matLibraryId) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addCqlConversionErrors(errors, matLibraryId);
    }

    public static void setMatCqlConversionExceptions(List<MatCqlConversionException> errors, String matLibraryId) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addMatCqlConversionErrors(errors, matLibraryId);
    }

    public static void setCql(String cql, String matLibraryId) {
        ConversionReporter conversionReporter = getConversionReporter();

        conversionReporter.addCql(cql, matLibraryId);
    }

    public static void setElm(String json, String matLibraryId) {
        ConversionReporter conversionReporter = getConversionReporter();

        conversionReporter.addElm(json, matLibraryId);
    }

    public static void resetValueSetResults() {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.clearValueSetResults();
    }

    public static void resetMeasure() {
        ConversionReporter conversionReporter = getConversionReporter();

        conversionReporter.clearMeasure();
    }

    public static void resetOrchestration() {
        ConversionReporter conversionReporter = getConversionReporter();

        conversionReporter.clearMeasureOrchestration();
    }

    public static ConversionReporter getConversionReporter() {
        ConversionReporter conversionReporter = getFromThreadLocal();

        if (conversionReporter == null) {
            throw new ThreadLocalNotFoundException(NOT_FOUND_THREAD_LOCAL_MESSAGE);
        }

        return conversionReporter;
    }

    public static ConversionResult getConversionResult() {
        ConversionReporter conversionReporter = getConversionReporter();
        return conversionReporter.findConversionResult();
    }

    public static void setValueSetInit(String oid, String reason) {
        ConversionReporter conversionReporter = getFromThreadLocal();

        if (conversionReporter != null) {
            conversionReporter.addValueSetResult(oid, null, null, reason);
        }
    }

    public static void setValueSetsValidationLink(String oid,
                                                  String link,
                                                  HapiResourcePersistedState state) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addValueSetResult(oid, Boolean.TRUE, link, state.value);
    }

    public static void removeInThreadLocalAndComplete() {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.complete();

        removeInThreadLocal();
    }

    public static void removeInThreadLocal() {
        threadLocal.remove();
    }

    public static void setInThreadLocal(String measureId, ConversionResultsService conversionResultsService) {
        removeInThreadLocal();
        threadLocal.set(new ConversionReporter(measureId, conversionResultsService));
    }

    static ConversionReporter getFromThreadLocal() {
        ConversionReporter conversionReporter = threadLocal.get();

        if (conversionReporter == null) {
            log.debug(NOT_FOUND_THREAD_LOCAL_MESSAGE);
        }
        return conversionReporter;
    }

    public static void setFhirMeasureValidationResults(List<FhirValidationResult> list) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addFhirMeasureValidationResults(list);
    }

    public static void setFhirLibraryValidationResults(List<FhirValidationResult> list, String matLibraryId) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addFhirMeasureLibraryResults(list, matLibraryId);
    }

    public static void setValueSetsValidationResults(String oid,
                                                     List<FhirValidationResult> list) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addValueSetValidationResults(oid, list);
    }

    public static void setValueSetsValidationError(String oid,
                                                   String error) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addValueSetResult(oid, Boolean.FALSE, null, error);
    }

    public static void setMeasureValidationLink(String link,
                                                HapiResourcePersistedState state) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addMeasureConversionResult(Boolean.TRUE, link, state.value);
    }

    public static void setLibraryValidationLink(String link,
                                                HapiResourcePersistedState reason,
                                                String matCqlId) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addLibraryConversionResult(link, reason.value, Boolean.TRUE, matCqlId);
    }

    public static void setLibraryValidationError(String reason,
                                                 String matCqlId) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addLibraryConversionResult(null, reason, Boolean.FALSE, matCqlId);
    }

    public static void setLibraryNotFoundInHapi(String matCqlId) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addLibraryConversionResult(null, "Not Found in Hapi", Boolean.FALSE, matCqlId);
    }

    public static void resetLibrary() {
        ConversionReporter conversionReporter = getConversionReporter();

        conversionReporter.clearLibrary();
    }

    public static void setTerminalMessage(String errorMessage, ConversionOutcome outcome) {
        try {
            ConversionReporter conversionReporter = getConversionReporter();
            conversionReporter.addErrorMessage(errorMessage, outcome);
            log.warn("Setting error message outcome: {}, message: {}", errorMessage, outcome);
        } catch (Exception e) {
            log.warn("Cannot find ConversionReporter: {}, outcome: {} setting error message: {} ",
                    e.getMessage(),
                    outcome,
                    errorMessage);
        }
    }

    private void addErrorMessage(String message, ConversionOutcome outcome) {
        conversionResultsService.addErrorMessage(measureId, message, outcome);
    }

    private void addMeasureResult(String field, String destination, String reason) {
        FieldConversionResult result =
                FieldConversionResult.builder()
                        .field(field)
                        .destination(destination)
                        .reason(reason)
                        .build();

        conversionResultsService.addMeasureResult(measureId, result);
    }

    private void addLibraryFieldConversionResult(String field, String destination, String reason, String matCqlId) {
        FieldConversionResult result = FieldConversionResult.builder()
                .field(field)
                .destination(destination)
                .reason(reason)
                .build();

        conversionResultsService.addLibraryFieldConversionResult(measureId, result, matCqlId);
    }

    private void clearValueSetResults() {
        conversionResultsService.clearValueSetResults(measureId);
    }

    private void clearMeasureOrchestration() {
        conversionResultsService.clearMeasureOrchestration(measureId);
    }

    private void clearMeasure() {
        conversionResultsService.clearMeasure(measureId);
    }

    private void clearLibrary() {
        conversionResultsService.clearLibrary(measureId);
    }

    private void addCqlConversionResultSuccess(String matLibraryId) {
        conversionResultsService.addCqlConversionResultSuccess(measureId, matLibraryId);
    }

    private void addCqlConversionErrorMessage(String error, String matLibraryId) {
        conversionResultsService.addCqlConversionErrorMessage(measureId, error, matLibraryId);
    }

    private void addCql(String cql, String matLibraryId) {
        conversionResultsService.addCql(measureId, cql, matLibraryId);
    }

    private void addElm(String json, String matLibraryId) {
        conversionResultsService.addElm(measureId, json, matLibraryId);
    }

    private void addCqlConversionErrors(List<CqlConversionError> errors, String matLibraryId) {
        conversionResultsService.addCqlConversionErrors(measureId, errors, matLibraryId);
    }

    private void addMatCqlConversionErrors(List<MatCqlConversionException> errors, String matLibraryId) {
        conversionResultsService.addMatCqlConversionErrors(measureId, errors, matLibraryId);
    }

    private void addFhirMeasureValidationResults(List<FhirValidationResult> list) {
        conversionResultsService.addFhirMeasureValidationResults(measureId, list);
    }

    private void addFhirMeasureLibraryResults(List<FhirValidationResult> list, String matLibraryId) {
        conversionResultsService.addLibraryValidationResults(measureId, list, matLibraryId);
    }

    private void addValueSetValidationResults(String oid, List<FhirValidationResult> list) {
        conversionResultsService.addValueSetValidationResults(measureId, oid, list);
    }

    private void addValueSetResult(String oid, Boolean success, String link, String reason) {
        conversionResultsService.addValueSetResult(measureId, oid, reason, success, link);
    }

    private void addMeasureConversionResult(Boolean success, String link, String reason) {
        conversionResultsService.addMeasureConversionResult(measureId, link, reason, success);
    }

    private void addFhirMeasureJson(String json) {
        conversionResultsService.addFhirMeasureJson(measureId, json);
    }

    private void addLibraryConversionResult(String link,
                                            String reason,
                                            Boolean success,
                                            String matLibraryId) {
        conversionResultsService.addLibraryConversionResult(measureId, link, reason, success, matLibraryId);
    }

    private ConversionResult findConversionResult() {
        return conversionResultsService.findConversionResult(measureId);
    }

    private void saveConversionResultToMongo(ConversionResult conversionResult) {
        conversionResultsService.save(conversionResult);
    }

    private void complete() {
        conversionResultsService.complete(measureId);
    }
}
