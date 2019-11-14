package gov.cms.mat.fhir.services.components.mongo;

import lombok.extern.slf4j.Slf4j;

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

    public static void resetLibrary() {
        ConversionReporter conversionReporter = getFromThreadLocal();

        if (conversionReporter == null) {
            throw new ThreadLocalNotFoundException(NOT_FOUND_THREAD_LOCAL_MESSAGE);
        }

        conversionReporter.clearLibrary();
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
        ConversionResult.ValueSetResult result = ConversionResult.ValueSetResult.builder()
                .oid(oid)
                .reason(reason)
                .build();

        return conversionResultsService.addValueSetResult(measureId, result);
    }

    private ConversionResult addMeasureResult(String field, String destination, String reason) {

        ConversionResult.MeasureResult result = ConversionResult.MeasureResult.builder()
                .field(field)
                .destination(destination)
                .reason(reason)
                .build();

        return conversionResultsService.addMeasureResult(measureId, result);
    }
    
    private ConversionResult addLibraryResult(String field, String destination, String reason) {

        ConversionResult.LibraryResult result = ConversionResult.LibraryResult.builder()
                .field(field)
                .destination(destination)
                .reason(reason)
                .build();

        return conversionResultsService.addLibraryResult(measureId, result);
    }
    

    private ConversionResult clearValueSetResults() {
        return conversionResultsService.clearValueSetResults(measureId);
    }

    private ConversionResult clearMeasure() {
        return conversionResultsService.clearMeasure(measureId);
    }
    
    private ConversionResult clearLibrary() {
        return conversionResultsService.clearLibrary(measureId);
    }
}
