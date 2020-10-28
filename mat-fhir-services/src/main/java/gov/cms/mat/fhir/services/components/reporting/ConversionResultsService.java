package gov.cms.mat.fhir.services.components.reporting;

import gov.cms.mat.fhir.rest.dto.ConversionOutcome;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.rest.dto.CqlConversionError;
import gov.cms.mat.fhir.rest.dto.FhirValidationResult;
import gov.cms.mat.fhir.rest.dto.FieldConversionResult;
import gov.cms.mat.fhir.rest.dto.LibraryConversionResults;
import gov.cms.mat.fhir.rest.dto.MatCqlConversionException;
import gov.cms.mat.fhir.rest.dto.MeasureConversionResults;
import gov.cms.mat.fhir.rest.dto.ValueSetConversionResults;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.LibraryConversionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class ConversionResultsService {
    private static final ThreadLocal<ConversionResult> threadLocal = new ThreadLocal<>();
    private final LibraryDataService libraryDataService;

    public ConversionResultsService(LibraryDataService libraryDataService) {
        this.libraryDataService = libraryDataService;
    }

    void addValueSetResult(ThreadSessionKey key,
                           String oid,
                           String reason,
                           Boolean success,
                           String link) {
        ConversionResult conversionResult = findOrCreate(key);

        ValueSetConversionResults valueSetConversionResults = conversionResult.findOrCreateValueSetConversionResults(oid);
        valueSetConversionResults.setSuccess(success);
        valueSetConversionResults.setReason(reason);
        valueSetConversionResults.setLink(link);
    }

    void addMeasureResult(ThreadSessionKey key, FieldConversionResult result) {
        ConversionResult conversionResult = findOrCreate(key);

        findOrCreateMeasureConversionResults(conversionResult).getMeasureResults().add(result);
    }

    public Optional<ConversionResult> findByThreadSessionKey(ThreadSessionKey key) {
        ConversionResult result = threadLocal.get();

        if (result == null) {
            log.error("Cannot find thread local value with key: {}", key);
            return Optional.empty();
        }

        return Optional.of(result);
    }

    public ConversionResult findConversionResult(ThreadSessionKey key) {
        Optional<ConversionResult> optional = findByThreadSessionKey(key);

        return optional
                .orElseThrow(() ->
                        new LibraryConversionException("Cannot find ConversionResult with key: " + key));
    }

    public void addCqlConversionResultSuccess(ThreadSessionKey key, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(key);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);
        libraryConversionResults.getCqlConversionResult().setResult(Boolean.TRUE);
    }

    public void addCqlConversionErrorMessage(ThreadSessionKey key, String error, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(key);

        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);
        libraryConversionResults.getCqlConversionResult().setResult(Boolean.FALSE);
        libraryConversionResults.getCqlConversionResult().getErrors().add(error);
    }


    public void addCql(ThreadSessionKey key, String cql, String name, String version, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(key);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);

        libraryConversionResults.setName(name);
        libraryConversionResults.setVersion(version);

        addLibraryData(key, cql, matLibraryId, LibraryType.QDM_CQL);
    }

    public void addFhirCql(ThreadSessionKey key, String cql, String matLibraryId) {
        addLibraryData(key, cql, matLibraryId, LibraryType.FHIR_CQL);
    }

    public void addFhirElmJson(ThreadSessionKey key, String json, String matLibraryId) {
        addLibraryData(key, json, matLibraryId, LibraryType.FHIR_ELM_JSON);
    }

    public void addFhirElmXml(ThreadSessionKey key, String xml, String matLibraryId) {
        addLibraryData(key, xml, matLibraryId, LibraryType.FHIR_ELM_XML);
    }

    public void addExternalLibraryErrors(ThreadSessionKey key, Map<String, List<CqlConversionError>> map, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(key);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);
        libraryConversionResults.setExternalErrors(map);
    }

    public void addFhirLibraryId(ThreadSessionKey key, String fhirLibraryId, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(key);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);
        libraryConversionResults.setFhirLibraryId(fhirLibraryId);

        conversionResult.getLibraryMappings().put(matLibraryId, fhirLibraryId);
    }

    public Optional<String> findFhirLibraryIdInMap(ThreadSessionKey key, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(key);

        String fhirLibraryId = conversionResult.getLibraryMappings().get(matLibraryId);

        if (StringUtils.isNotEmpty(fhirLibraryId)) {
            return Optional.of(fhirLibraryId);
        } else {
            return Optional.empty();
        }
    }


    private void addLibraryData(ThreadSessionKey key, String data, String matLibraryId, LibraryType type) {
        ConversionResult conversionResult = findOrCreate(key);

        libraryDataService.findOrCreate(
                conversionResult.getSourceMeasureId(),
                matLibraryId,
                type,
                data);
    }

    public String getCql(ThreadSessionKey key, String matLibraryId) {
        return getLibraryData(key, matLibraryId, LibraryType.QDM_CQL);
    }

    public String getElm(ThreadSessionKey key, String matLibraryId) {
        return getLibraryData(key, matLibraryId, LibraryType.QDM_ELM);
    }

    public String getFhirElmJson(ThreadSessionKey key, String matLibraryId) {
        return getLibraryData(key, matLibraryId, LibraryType.FHIR_ELM_JSON);
    }

    public String getFhirElmXml(ThreadSessionKey key, String matLibraryId) {
        return getLibraryData(key, matLibraryId, LibraryType.FHIR_ELM_XML);
    }

    public String getFhirCql(ThreadSessionKey key, String matLibraryId) {
        return getLibraryData(key, matLibraryId, LibraryType.FHIR_CQL);
    }

    private String getLibraryData(ThreadSessionKey key, String matLibraryId, LibraryType type) {
        ConversionResult conversionResult = findOrCreate(key);

        var optional = libraryDataService.findByIndex(conversionResult.getSourceMeasureId(), matLibraryId, type);

        return optional.map(LibraryData::getData).orElse(StringUtils.EMPTY);
    }

    public void addElm(ThreadSessionKey key, String json, String matLibraryId) {
        addLibraryData(key, json, matLibraryId, LibraryType.QDM_ELM);
    }

    public void addCqlConversionErrors(ThreadSessionKey key, List<CqlConversionError> errors, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(key);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);
        libraryConversionResults.getCqlConversionResult().setResult(Boolean.FALSE);
        libraryConversionResults.getCqlConversionResult().getCqlConversionErrors().addAll(errors);
    }

    public void addFhirCqlConversionErrors(ThreadSessionKey key, List<CqlConversionError> errors, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(key);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);
        libraryConversionResults.getCqlConversionResult().setResult(Boolean.FALSE);
        libraryConversionResults.getCqlConversionResult().getFhirCqlConversionErrors().addAll(errors);
    }

    public void addMatCqlConversionErrors(ThreadSessionKey key, List<MatCqlConversionException> errors, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(key);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);
        libraryConversionResults.getCqlConversionResult().setResult(Boolean.FALSE);
        libraryConversionResults.getCqlConversionResult().getMatCqlConversionErrors().addAll(errors);

    }

    public void addFhirMatCqlConversionErrors(ThreadSessionKey key, List<MatCqlConversionException> errors, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(key);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);
        libraryConversionResults.getCqlConversionResult().setResult(Boolean.FALSE);
        libraryConversionResults.getCqlConversionResult().getFhirMatCqlConversionErrors().addAll(errors);
    }

    public void addFhirMeasureValidationResults(ThreadSessionKey key,
                                                List<FhirValidationResult> list) {
        ConversionResult conversionResult = findOrCreate(key);

        findOrCreateMeasureConversionResults(conversionResult).setMeasureFhirValidationResults(list);
    }

    public void addLibraryValidationResults(ThreadSessionKey key,
                                            List<FhirValidationResult> list,
                                            String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(key);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);

        libraryConversionResults.getLibraryFhirValidationResults().addAll(list);
    }

    public void addValueSetJson(ThreadSessionKey key, String oid, String json) {
        ConversionResult conversionResult = findOrCreate(key);

        ValueSetConversionResults valueSetConversionResults = conversionResult.findOrCreateValueSetConversionResults(oid);
        valueSetConversionResults.setJson(json);
    }

    public void addValueSetValidationResults(ThreadSessionKey key,
                                             String oid,
                                             List<FhirValidationResult> list) {
        ConversionResult conversionResult = findOrCreate(key);
        ValueSetConversionResults valueSetConversionResults = conversionResult.findOrCreateValueSetConversionResults(oid);
        valueSetConversionResults.getValueSetFhirValidationResults().addAll(list);
    }


    public void addLibraryConversionResult(ThreadSessionKey key,
                                           String link,
                                           String message,
                                           Boolean success,
                                           String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(key);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);

        libraryConversionResults.setLink(link);
        libraryConversionResults.setReason(message);
        libraryConversionResults.setSuccess(success);
    }

    public void addErrorMessage(ThreadSessionKey key, String message, ConversionOutcome outcome) {
        ConversionResult conversionResult = findOrCreate(key);
        conversionResult.setErrorReason(message);
        conversionResult.setOutcome(outcome);
    }

    public void addValueSetProcessingMemo(ThreadSessionKey key, String memo) {
        ConversionResult conversionResult = findOrCreate(key);
        conversionResult.setValueSetProcessingMemo(memo);
        conversionResult.setValueSetsProcessed(Instant.now());
    }

    public void addFhirMeasureJson(ThreadSessionKey key, String json) {
        ConversionResult conversionResult = findOrCreate(key);

        findOrCreateMeasureConversionResults(conversionResult).setFhirMeasureJson(json);
    }

    private MeasureConversionResults findOrCreateMeasureConversionResults(ConversionResult conversionResult) {
        if (conversionResult.getMeasureConversionResults() == null) {
            conversionResult.setMeasureConversionResults(new MeasureConversionResults());
        }

        return conversionResult.getMeasureConversionResults();
    }

    public void addMeasureConversionResult(ThreadSessionKey key,
                                           String link,
                                           String reason,
                                           Boolean success) {

        ConversionResult conversionResult = findOrCreate(key);

        MeasureConversionResults measureConversionResults = findOrCreateMeasureConversionResults(conversionResult);

        measureConversionResults.setLink(link);
        measureConversionResults.setReason(reason);
        measureConversionResults.setSuccess(success);
    }

    public void complete(ThreadSessionKey key) {
        ConversionResult conversionResult = findOrCreate(key);
        conversionResult.setFinished(Instant.now());
    }

    public void addConversionType(ThreadSessionKey key, ConversionType conversionType) {
        ConversionResult conversionResult = findOrCreate(key);
        conversionResult.setConversionType(conversionType);
    }

    public void addBatchId(ThreadSessionKey key, String batchId) {
        ConversionResult conversionResult = findOrCreate(key);
        conversionResult.setBatchId(batchId);
    }

    public void addXmlSource(ThreadSessionKey key, XmlSource xmlSource) {
        ConversionResult conversionResult = findOrCreate(key);
        conversionResult.setXmlSource(xmlSource);
    }

    public void addMeasureLibraryId(ThreadSessionKey key, String id) {
        ConversionResult conversionResult = findOrCreate(key);
        conversionResult.setFhirMeasureId(id);
    }

    public void addShowWarnings(ThreadSessionKey key, boolean flag) {
        ConversionResult conversionResult = findOrCreate(key);
        conversionResult.setShowWarnings(flag);
    }

    public void addVsacGrantingTicket(ThreadSessionKey key, String vsacGrantingTicket) {
        ConversionResult conversionResult = findOrCreate(key);
        conversionResult.setVsacGrantingTicket(vsacGrantingTicket);
    }

    private ConversionResult findOrCreate(ThreadSessionKey key) {
        ConversionResult result = threadLocal.get();

        if (result == null) {
            result = new ConversionResult();
            result.setSourceMeasureId(key.getMeasureId());
            result.setStart(key.getStart());
            threadLocal.set(result);
        }

        return result;
    }

    void clear() {
        threadLocal.remove();
    }
}
