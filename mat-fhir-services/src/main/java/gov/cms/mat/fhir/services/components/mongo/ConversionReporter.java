package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.cql.*;
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

    public static void setLibraryResult(String field, String destination, String reason) {
        ConversionReporter conversionReporter = getFromThreadLocal();

        if (conversionReporter != null) {
            conversionReporter.addLibraryResult(field, destination, reason);
        }
    }

    public static void setCqlConversionResultSuccess() {
        ConversionReporter conversionReporter = getConversionReporter();

        conversionReporter.addCqlConversionResultSuccess();
    }

    public static void setCqlConversionErrorMessage(String error) {
        ConversionReporter conversionReporter = getConversionReporter();

        conversionReporter.addCqlConversionErrorMessage(error);
    }

    public static void setCqlConversionErrors(List<CqlConversionError> errors) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addCqlConversionErrors(errors);
    }

    public static void setMatCqlConversionExceptions(List<MatCqlConversionException> errors) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addMatCqlConversionErrors(errors);
    }


    public static void setCql(String cql) {
        ConversionReporter conversionReporter = getConversionReporter();

        conversionReporter.addCql(cql);
    }

    public static void setElm(String json) {
        ConversionReporter conversionReporter = getConversionReporter();

        conversionReporter.addElm(json);
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

    public static void setValueSetFailResult(String oid, String reason) {
        ConversionReporter conversionReporter = getFromThreadLocal();

        if (conversionReporter != null) {
            conversionReporter.addValueSetFailResult(oid, reason);
        }
    }

    public static void setValueSetSuccessResult(String oid) {
        ConversionReporter conversionReporter = getFromThreadLocal();

        if (conversionReporter != null) {
            conversionReporter.addValueSetSuccessResult(oid);
        }
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

    public static void setFhirLibraryValidationResults(List<FhirValidationResult> list) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addFhirMeasureLibraryResults(list);
    }

    public static void setValueSetsValidationResults(String oid,
                                                     List<FhirValidationResult> list) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addValueSetValidationResults(oid, list);
    }

    public static void resetLibrary(ConversionType conversionType) {
        ConversionReporter conversionReporter = getConversionReporter();

        conversionReporter.clearLibrary(conversionType);
    }

    private void addValueSetFailResult(String oid, String reason) {
        ValueSetResult result =
                ValueSetResult.builder()
                        .oid(oid)
                        .reason(reason)
                        .success(false)
                        .build();

        conversionResultsService.addValueSetResult(measureId, result);
    }

    private void addValueSetSuccessResult(String oid) {
        ValueSetResult result =
                ValueSetResult.builder()
                        .oid(oid)
                        .success(true)
                        .build();

        conversionResultsService.addValueSetResult(measureId, result);
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

    private void addLibraryResult(String field, String destination, String reason) {

        FieldConversionResult result = FieldConversionResult.builder()
                .field(field)
                .destination(destination)
                .reason(reason)
                .build();

        conversionResultsService.addLibraryResult(measureId, result);
    }


    private void clearValueSetResults(ConversionType conversionType) {
        conversionResultsService.clearValueSetResults(measureId);
        conversionResultsService.setValueSetConversionType(measureId, conversionType);
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
        conversionResultsService.clearCqlConversionResult(measureId);
        conversionResultsService.setCqlConversionResult(measureId, conversionType);
    }

    private void addCqlConversionResultSuccess() {
        conversionResultsService.addCqlConversionResultSuccess(measureId);
    }

    private void addCqlConversionErrorMessage(String error) {
        conversionResultsService.addCqlConversionErrorMessage(measureId, error);
    }

    private void addCql(String cql) {
        conversionResultsService.addCql(measureId, cql);
    }

    private void addElm(String json) {
        conversionResultsService.addElm(measureId, json);
    }

    private void addCqlConversionErrors(List<CqlConversionError> errors) {
        conversionResultsService.addCqlConversionErrors(measureId, errors);
    }

    private void addMatCqlConversionErrors(List<MatCqlConversionException> errors) {
        conversionResultsService.addMatCqlConversionErrors(measureId, errors);
    }

    private void addFhirMeasureValidationResults(List<FhirValidationResult> list) {
        conversionResultsService.addFhirMeasureValidationResults(measureId, list);
    }

    private void addFhirMeasureLibraryResults(List<FhirValidationResult> list) {
        conversionResultsService.addLibraryValidationResults(measureId, list);
    }

    private void addValueSetValidationResults(String oid, List<FhirValidationResult> list) {
        conversionResultsService.addValueSetValidationResults(measureId, oid, list);
    }

    private ConversionResult findConversionResult() {
        return conversionResultsService.findConversionResult(measureId);
    }
}
