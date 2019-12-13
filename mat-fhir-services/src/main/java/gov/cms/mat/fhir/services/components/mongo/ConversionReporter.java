package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.services.service.support.CqlConversionError;
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


    public static void setCql(String cql) {
        ConversionReporter conversionReporter = getConversionReporter();

        conversionReporter.addCql(cql);
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

    public static ConversionResult resetLibrary(ConversionType conversionType) {
        ConversionReporter conversionReporter = getConversionReporter();

        return conversionReporter.clearLibrary(conversionType);
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


    public static void setValueSetResult(String oid, String reason) {
        ConversionReporter conversionReporter = getFromThreadLocal();

        if (conversionReporter != null) {
            conversionReporter.addValueSetResult(oid, reason);
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


    private ConversionResult addValueSetResult(String oid, String reason) {
        ConversionResult.ValueSetResult result = gov.cms.mat.fhir.services.components.mongo.ConversionResult.ValueSetResult.builder()
                .oid(oid)
                .reason(reason)
                .build();

        return conversionResultsService.addValueSetResult(measureId, result);
    }

    private ConversionResult addMeasureResult(String field, String destination, String reason) {

        ConversionResult.FieldConversionResult result = ConversionResult.FieldConversionResult.builder()
                .field(field)
                .destination(destination)
                .reason(reason)
                .build();

        return conversionResultsService.addMeasureResult(measureId, result);
    }

    private ConversionResult addLibraryResult(String field, String destination, String reason) {

        ConversionResult.FieldConversionResult result = ConversionResult.FieldConversionResult.builder()
                .field(field)
                .destination(destination)
                .reason(reason)
                .build();

        return conversionResultsService.addLibraryResult(measureId, result);
    }


    private ConversionResult clearValueSetResults(ConversionType conversionType) {
        conversionResultsService.clearValueSetResults(measureId);
        return conversionResultsService.setValueSetConversionType(measureId, conversionType);
    }

    private ConversionResult clearMeasure(ConversionType conversionType) {
        conversionResultsService.clearMeasure(measureId);
        return conversionResultsService.setMeasureConversionType(measureId, conversionType);
    }

    private ConversionResult clearLibrary(ConversionType conversionType) {
        conversionResultsService.clearLibrary(measureId);
        return conversionResultsService.setLibraryConversionType(measureId, conversionType);
    }

    private ConversionResult clearCqlConversionResult(ConversionType conversionType) {
        conversionResultsService.clearCqlConversionResult(measureId);
        return conversionResultsService.setCqlConversionResult(measureId, conversionType);
    }

    private ConversionResult addCqlConversionResultSuccess() {
        return conversionResultsService.addCqlConversionResultSuccess(measureId);
    }

    private ConversionResult addCqlConversionErrorMessage(String error) {
        return conversionResultsService.addCqlConversionErrorMessage(measureId, error);
    }

    private ConversionResult addCql(String cql) {
        return conversionResultsService.addCql(measureId, cql);
    }

    private ConversionResult addCqlConversionErrors(List<CqlConversionError> errors) {
        return conversionResultsService.addCqlConversionErrors(measureId, errors);
    }

    private ConversionResult findConversionResult() {
        return conversionResultsService.findConversionResult(measureId);
    }

}
