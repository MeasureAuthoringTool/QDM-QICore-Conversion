package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.dto.*;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.ThreadLocalNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Slf4j
public class ConversionReporter {
    private static final String NOT_FOUND_THREAD_LOCAL_MESSAGE = "Cannot get (null)conversionReporter from threadLocal";
    private static final ThreadLocal<ConversionReporter> threadLocal = new ThreadLocal<>();

    private final ThreadSessionKey key;
    private final ConversionResultsService conversionResultsService;

    private ConversionReporter(String measureId, ConversionResultsService conversionResultsService, Instant start) {
        this.conversionResultsService = conversionResultsService;

        key = ThreadSessionKey.builder()
                .measureId(measureId)
                .start(start)
                .build();
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

    public static void setFhirCqlConversionErrors(List<CqlConversionError> errors, String matLibraryId) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addFhirCqlConversionErrors(errors, matLibraryId);
    }

    public static void setMatCqlConversionExceptions(List<MatCqlConversionException> errors, String matLibraryId) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addMatCqlConversionErrors(errors, matLibraryId);
    }


    public static void setFhirMatCqlConversionExceptions(List<MatCqlConversionException> errors, String matLibraryId) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addFhirMatCqlConversionErrors(errors, matLibraryId);
    }

    public static void setCql(String cql, String name, BigDecimal version, String matLibraryId) {
        ConversionReporter conversionReporter = getConversionReporter();

        String versionString = version == null ? "null" : version.toString();

        conversionReporter.addCql(cql, name, versionString, matLibraryId);
    }

    public static void setCqlNameAndVersion(String name, String version, String matLibraryId) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addCqlNameAndVersion(name, version, matLibraryId);
    }

    public static void setFhirCql(String fhirCql, String matLibraryId) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addFhirCql(fhirCql, matLibraryId);
    }

    public static void setFhirJson(String fhirJson, String matLibraryId) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addFhirJson(fhirJson, matLibraryId);
    }

    public static String getCql(String matLibraryId) {
        ConversionReporter conversionReporter = getConversionReporter();

        return conversionReporter.getCqlString(matLibraryId);
    }

    public static void setElm(String json, String matLibraryId) {
        ConversionReporter conversionReporter = getConversionReporter();

        conversionReporter.addElm(json, matLibraryId);
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

    public static void setValueSetInit(String oid, String reason, Boolean success) {
        ConversionReporter conversionReporter = getFromThreadLocal();

        if (conversionReporter != null) {
            conversionReporter.addValueSetResult(oid, success, null, reason);
        }
    }

    public static void setValueSetsValidationLink(String oid,
                                                  String link,
                                                  HapiResourcePersistedState state) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addValueSetResult(oid, Boolean.TRUE, link, state.value);
    }

    public static void setValueSetJson(String oid, String json) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addValueSetJson(oid, json);
    }

    public static void removeInThreadLocalAndComplete() {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.complete();

        removeInThreadLocal();
    }

    public static void removeInThreadLocal() {
        threadLocal.remove();
    }

    public static ThreadSessionKey setInThreadLocal(String measureId,
                                                    String batchId,
                                                    ConversionResultsService conversionResultsService,
                                                    Instant instant,
                                                    ConversionType conversionType,
                                                    XmlSource xmlSource,
                                                    boolean showWarnings,
                                                    String vsacGrantingTicket) {
        removeInThreadLocal();
        threadLocal.set(new ConversionReporter(measureId, conversionResultsService, instant));
        setConversionType(conversionType);
        setBatchId(batchId);
        setXmlSource(xmlSource);
        setShowWarnings(showWarnings);

        return getKey();
    }

    static ThreadSessionKey getKey() {
        return getConversionReporter().key;
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

        Boolean result = Boolean.TRUE;

        if (StringUtils.isEmpty(link)) {
            result = null;
        }

        conversionReporter.addMeasureConversionResult(result, link, state.value);
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

    public static void setTerminalMessage(String errorMessage, ConversionOutcome outcome) {
        try {
            ConversionReporter conversionReporter = getConversionReporter();
            conversionReporter.addErrorMessage(errorMessage, outcome);
            log.debug("Setting error message outcome: {}, message: {}", errorMessage, outcome);
        } catch (Exception e) {
            log.warn("Cannot find ConversionReporter: {}, outcome: {} setting error message: {} ",
                    e.getMessage(),
                    outcome,
                    errorMessage);
        }
    }

    public static void setValueSetCompletionMemo(String memo) {
        try {
            ConversionReporter conversionReporter = getConversionReporter();
            conversionReporter.addValueSetProcessingMemo(memo);
            log.debug("Setting memo: {}", memo);
        } catch (Exception e) {
            log.warn("Cannot find ConversionReporter: {} setting valueSetCompletionMemo: {}",
                    e.getMessage(),
                    memo);
        }
    }

    public static void setConversionType(ConversionType conversionType) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addConversionType(conversionType);
    }

    public static void setBatchId(String batchId) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addBatchId(batchId);
    }

    public static void setXmlSource(XmlSource xmlSource) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addXmlSource(xmlSource);
    }

    public static void setShowWarnings(boolean flag) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addShowWarnings(flag);
    }

    public static void setShowWarnings(String vsacGrantingTicket) {
        ConversionReporter conversionReporter = getConversionReporter();
        conversionReporter.addVsacGrantingTicket(vsacGrantingTicket);
    }

    private void addBatchId(String batchId) {
        conversionResultsService.addBatchId(key, batchId);
    }

    private void addXmlSource(XmlSource xmlSource) {
        conversionResultsService.addXmlSource(key, xmlSource);
    }

    private void addShowWarnings(boolean flag) {
        conversionResultsService.addShowWarnings(key, flag);
    }

    private void addVsacGrantingTicket(String vsacGrantingTicket) {
        conversionResultsService.addVsacGrantingTicket(key, vsacGrantingTicket);
    }

    private void addErrorMessage(String message, ConversionOutcome outcome) {
        conversionResultsService.addErrorMessage(key, message, outcome);
    }

    private void addValueSetProcessingMemo(String memo) {
        conversionResultsService.addValueSetProcessingMemo(key, memo);
    }

    private void addConversionType(ConversionType conversionType) {
        conversionResultsService.addConversionType(key, conversionType);
    }

    private void addMeasureResult(String field, String destination, String reason) {
        FieldConversionResult result =
                FieldConversionResult.builder()
                        .field(field)
                        .destination(destination)
                        .reason(reason)
                        .build();

        conversionResultsService.addMeasureResult(key, result);
    }

//    private void addLibraryFieldConversionResult(String field, String destination, String reason, String matCqlId) {
//        FieldConversionResult result = FieldConversionResult.builder()
//                .field(field)
//                .destination(destination)
//                .reason(reason)
//                .build();
//
//        conversionResultsService.addLibraryFieldConversionResult(key, result, matCqlId);
//    }


    private void addCqlConversionResultSuccess(String matLibraryId) {
        conversionResultsService.addCqlConversionResultSuccess(key, matLibraryId);
    }

    private void addCqlConversionErrorMessage(String error, String matLibraryId) {
        conversionResultsService.addCqlConversionErrorMessage(key, error, matLibraryId);
    }

    private void addCql(String cql, String name, String version, String matLibraryId) {
        conversionResultsService.addCql(key, cql, name, version, matLibraryId);
    }

    private void addCqlNameAndVersion(String name, String version, String matLibraryId) {
        conversionResultsService.addCql(key, null, name, version, matLibraryId);
    }

    private void addFhirCql(String cql, String matLibraryId) {
        conversionResultsService.addFhirCql(key, cql, matLibraryId);
    }

    private void addFhirJson(String json, String matLibraryId) {
        conversionResultsService.addFhirJson(key, json, matLibraryId);
    }

    private String getCqlString(String matLibraryId) {
        return conversionResultsService.getCql(key, matLibraryId);
    }

    private void addElm(String json, String matLibraryId) {
        conversionResultsService.addElm(key, json, matLibraryId);
    }

    private void addCqlConversionErrors(List<CqlConversionError> errors, String matLibraryId) {
        conversionResultsService.addCqlConversionErrors(key, errors, matLibraryId);
    }

    private void addFhirCqlConversionErrors(List<CqlConversionError> errors, String matLibraryId) {
        conversionResultsService.addFhirCqlConversionErrors(key, errors, matLibraryId);
    }

    private void addMatCqlConversionErrors(List<MatCqlConversionException> errors, String matLibraryId) {
        conversionResultsService.addMatCqlConversionErrors(key, errors, matLibraryId);
    }

    private void addFhirMatCqlConversionErrors(List<MatCqlConversionException> errors, String matLibraryId) {
        conversionResultsService.addFhirMatCqlConversionErrors(key, errors, matLibraryId);
    }

    private void addFhirMeasureValidationResults(List<FhirValidationResult> list) {
        conversionResultsService.addFhirMeasureValidationResults(key, list);
    }

    private void addFhirMeasureLibraryResults(List<FhirValidationResult> list, String matLibraryId) {
        conversionResultsService.addLibraryValidationResults(key, list, matLibraryId);
    }

    private void addValueSetValidationResults(String oid, List<FhirValidationResult> list) {
        conversionResultsService.addValueSetValidationResults(key, oid, list);
    }

    private void addValueSetResult(String oid, Boolean success, String link, String reason) {
        conversionResultsService.addValueSetResult(key, oid, reason, success, link);
    }

    private void addValueSetJson(String oid, String json) {
        conversionResultsService.addValueSetJson(key, oid, json);
    }

    private void addMeasureConversionResult(Boolean success, String link, String reason) {
        conversionResultsService.addMeasureConversionResult(key, link, reason, success);
    }

    private void addFhirMeasureJson(String json) {
        conversionResultsService.addFhirMeasureJson(key, json);
    }

    private void addLibraryConversionResult(String link,
                                            String reason,
                                            Boolean success,
                                            String matLibraryId) {
        conversionResultsService.addLibraryConversionResult(key, link, reason, success, matLibraryId);
    }

    private ConversionResult findConversionResult() {
        return conversionResultsService.findConversionResult(key);
    }

    private void saveConversionResultToMongo(ConversionResult conversionResult) {
        conversionResultsService.save(conversionResult);
    }

    private void complete() {
        conversionResultsService.complete(key);
    }
}
