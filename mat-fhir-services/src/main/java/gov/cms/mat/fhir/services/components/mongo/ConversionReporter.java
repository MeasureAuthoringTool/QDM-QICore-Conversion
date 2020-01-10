package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.dto.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ConversionReporter {
    private static final String NOT_FOUND_THREAD_LOCAL_MESSAGE = "Cannot get (null)conversionReporter from threadLocal";

    private static final ThreadLocal<ConversionReporter> threadLocal = new ThreadLocal<>();

    private final String measureId;
    private final ConversionResultsService conversionResultsService;

    private ConversionReporter(String measureId, ConversionResultsService conversionResultsService) {
        this.measureId = measureId;
        this.conversionResultsService = conversionResultsService;
    }

    public static void setMeasureResult(String field, String destination, String reason) {
        ConversionReporter conversionReporter = getFromThreadLocal();

        if (conversionReporter != null) {
            conversionReporter.addMeasureResult(field, destination, reason);
        }
    }

    public static void setLibraryResult(String field, String destination, String reason, String matLibraryId) {
        ConversionReporter conversionReporter = getFromThreadLocal();

        if (conversionReporter != null) {
            conversionReporter.addLibraryResult(field, destination, reason, matLibraryId);
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


    public static void resetCqlConversionResult(ConversionType conversionType) {
        ConversionReporter conversionReporter = getConversionReporter();

        conversionReporter.clearCqlConversionResult(conversionType);
    }

    public static void resetValueSetResults(ConversionType conversionType) {
        ConversionReporter conversionReporter = getConversionReporter();

        conversionReporter.clearValueSetResults(conversionType);
    }

    public static void resetMeasure(ConversionType conversionType) {
        ConversionReporter conversionReporter = getConversionReporter();

        conversionReporter.clearMeasure(conversionType);
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

    public static void saveConversionResult(ConversionResult conversionResult) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.saveConversionResultToMongo(conversionResult);
    }

    public static void setValueSetInit(String oid, String reason) {
        ConversionReporter conversionReporter = getFromThreadLocal();

        if (conversionReporter != null) {
            conversionReporter.addValueSetInit(oid, reason);
        }
    }

    public static void setValueSetsValidationLink(String oid,
                                                  String link,
                                                  String reason) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addValueSetValidationLink(oid, link, reason);
    }

    static void removeInThreadLocal() {
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

    public static void setValueSetsValidationResult(String oid,
                                                    FhirValidationResult fhirValidationResult) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addValueSetValidationResult(oid, fhirValidationResult);
    }

    public static void setValueSetsValidationError(String oid,
                                                   String error) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addValueSetValidationError(oid, error);
    }




    public static void setLibraryValidationLink(String link,
                                                String reason,
                                                String matCqlId) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addLibraryValidationLink(link, reason, matCqlId);
    }

    public static void setLibraryValidationError(String reason,
                                                 String matCqlId) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addLibraryValidationError(reason, matCqlId);
    }

    public static void setLibraryNotFoundInHapi(String matCqlId) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addLibraryNotFoundInHapi(matCqlId);
    }

    public static void resetLibrary(ConversionType conversionType) {
        ConversionReporter conversionReporter = getConversionReporter();

        conversionReporter.clearLibrary(conversionType);
    }

    public static void setErrorMessage(String errorMessage) {
        try {
            ConversionReporter conversionReporter = getConversionReporter();
            conversionReporter.addErrorMessage(errorMessage);
        } catch (Exception e) {
            log.warn("Cannot find ConversionReporter: {}, setting error message: {} ", e.getMessage(), errorMessage);
        }
    }

    private void addErrorMessage(String message) {
        conversionResultsService.addErrorMessage(measureId, message);
    }

    private void addValueSetInit(String oid, String reason) {
        conversionResultsService.addValueSetResult(measureId, oid, reason, null, null);
    }

    private void addValueSetSuccessResult(String oid, String reason, String link) {
        //todo add link and result
        conversionResultsService.addValueSetResult(measureId, oid, reason, Boolean.TRUE, link);
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

    private void addLibraryResult(String field, String destination, String reason, String matCqlId) {

        FieldConversionResult result = FieldConversionResult.builder()
                .field(field)
                .destination(destination)
                .reason(reason)
                .build();

        conversionResultsService.addLibraryResult(measureId, result, matCqlId);
    }


    private void clearValueSetResults(ConversionType conversionType) {
        conversionResultsService.clearValueSetResults(measureId);
    }

    private void clearMeasureOrchestration() {
        conversionResultsService.clearMeasureOrchestration(measureId);
    }


    private void clearMeasure(ConversionType conversionType) {
        conversionResultsService.clearMeasure(measureId);
        conversionResultsService.setMeasureConversionType(measureId, conversionType);
    }

    private void clearLibrary(ConversionType conversionType) {
        conversionResultsService.clearLibrary(measureId);
        conversionResultsService.setLibraryConversionType(measureId, conversionType);
    }

    private void clearCqlConversionResult(ConversionType conversionType) {
        // conversionResultsService.clearCqlConversionResult(measureId);
        conversionResultsService.setCqlConversionResult(measureId, conversionType);
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

    private void addValueSetValidationResult(String oid, FhirValidationResult fhirValidationResult) {
        conversionResultsService.addValueSetValidationResult(measureId, oid, fhirValidationResult);
    }

    private void addValueSetValidationError(String oid, String error) {
        conversionResultsService.addValueSetValidation(measureId, oid, error, null, Boolean.FALSE);
    }

    private void addValueSetValidationLink(String oid, String link, String reason) {
        conversionResultsService.addValueSetValidation(measureId, oid, link, reason, Boolean.TRUE);
    }

    private void addLibraryValidationLink(String link, String reason, String matLibraryId) {
        conversionResultsService.addLibraryValidationLink(measureId, link, reason, matLibraryId);
    }

    private void addLibraryValidationError(String reason, String matLibraryId) {
        conversionResultsService.addLibraryValidationError(measureId, reason, matLibraryId);
    }

    private void addLibraryNotFoundInHapi(String matLibraryId) {
        conversionResultsService.addLibraryNotFoundInHapi(measureId, matLibraryId);
    }

    private void saveConversionResultToMongo(ConversionResult conversionResult) {
        conversionResultsService.save(conversionResult);
    }

    private ConversionResult findConversionResult() {
        return conversionResultsService.findConversionResult(measureId);
    }
}
