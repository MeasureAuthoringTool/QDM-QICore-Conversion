package gov.cms.mat.fhir.services.components.mongo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConversionReporter {
    private static final String NOT_FOUND_THREAD_LOCAL_MESSAGE = "Cannot get (null)conversionReporter from threadLocal";

    private static final ThreadLocal<ConversionReporter> threadLocal = new ThreadLocal<>();

    private final String measureId;
    private final ConversionResultsService conversionResultsService;


    public ConversionReporter(String measureId, ConversionResultsService conversionResultsService) {
        this.measureId = measureId;
        this.conversionResultsService = conversionResultsService;
    }

    public static void setMeasureResult(String field, String reason) {
        ConversionReporter conversionReporter = getFromThreadLocal();

        if (conversionReporter != null) {
            conversionReporter.addMeasureResult(field, reason);
        }
    }

    public static void resetValueSetResults() {
        ConversionReporter conversionReporter = getFromThreadLocal();

        if (conversionReporter == null) {
            throw new ThreadLocalNotFoundException(NOT_FOUND_THREAD_LOCAL_MESSAGE);
        }

        conversionReporter.clearValueSetResults();
    }

    public static void resetMeasure() {
        ConversionReporter conversionReporter = getFromThreadLocal();

        if (conversionReporter == null) {
            throw new ThreadLocalNotFoundException(NOT_FOUND_THREAD_LOCAL_MESSAGE);
        }

        conversionReporter.clearMeasure();
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

    private static ConversionReporter getFromThreadLocal() {
        ConversionReporter conversionReporter = threadLocal.get();

        if (conversionReporter == null) {
            log.debug(NOT_FOUND_THREAD_LOCAL_MESSAGE);
        }
        return conversionReporter;
    }

    public ConversionResult addValueSetResult(String oid, String reason) {
        ConversionResult.ValueSetResult result = ConversionResult.ValueSetResult.builder()
                .oid(oid)
                .reason(reason)
                .build();

        return conversionResultsService.addValueSetResult(measureId, result);
    }

    public ConversionResult addMeasureResult(String field, String reason) {

        ConversionResult.MeasureResult result = ConversionResult.MeasureResult.builder()
                .field(field)
                .reason(reason)
                .build();

        return conversionResultsService.addMeasureResult(measureId, result);
    }

    public ConversionResult clearValueSetResults() {
        return conversionResultsService.clearValueSetResults(measureId);
    }

    public ConversionResult clearMeasure() {
        return conversionResultsService.clearMeasure(measureId);
    }
}
