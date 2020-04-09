package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.dto.*;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.LibraryConversionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ConversionResultsService {
    private final ConversionResultRepository conversionResultRepository;
    private final LibraryDataService libraryDataService;

    public ConversionResultsService(ConversionResultRepository conversionResultRepository,
                                    LibraryDataService libraryDataService) {
        this.conversionResultRepository = conversionResultRepository;
        this.libraryDataService = libraryDataService;
    }

    public Optional<ConversionResult> findTopValueSetConversion() {
        return conversionResultRepository.findTop1ByValueSetsProcessedIsNullAndFinishedIsNullOrderByCreated();
    }

    public boolean checkBatchIdNotUsed(String batchId) {
        return conversionResultRepository.countByBatchId(batchId) == 0;
    }

    public boolean checkBatchIdUsed(String batchId) {
        return conversionResultRepository.countByBatchId(batchId) > 0;
    }

    public List<ConversionResult> findByBatchId(String batchId) {
        return conversionResultRepository.findByBatchId(batchId);
    }


    public Set<String> findBatchIds() {
        List<ConversionResult> conversionResults = conversionResultRepository.findAllBatchIds();

        return conversionResults.stream()
                .map(ConversionResult::getBatchId)
                .collect(Collectors.toSet());
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


        save(conversionResult);
    }

    void addMeasureResult(ThreadSessionKey key, FieldConversionResult result) {
        ConversionResult conversionResult = findOrCreate(key);

        findOrCreateMeasureConversionResults(conversionResult).getMeasureResults().add(result);

        save(conversionResult);
    }


    public Optional<ConversionResult> findByThreadSessionKey(ThreadSessionKey key) {
        return conversionResultRepository.findBySourceMeasureIdAndStart(key.getMeasureId(), key.getStart());
    }

    public List<ConversionResult> findAllBySourceMeasureId(String measureId) {
        return conversionResultRepository.findBySourceMeasureId(measureId);
    }

    public Optional<ConversionResult> findTopBySourceMeasureId(String measureId) {
        return conversionResultRepository.findTopBySourceMeasureIdOrderByCreatedDesc(measureId);
    }

    public List<ConversionResult> findAll() {
        return conversionResultRepository.findAll(Sort.by(Sort.Direction.DESC, "modified"));
    }

    private ConversionResult findOrCreate(ThreadSessionKey key) {
        Optional<ConversionResult> optional = findByThreadSessionKey(key);

        if (optional.isPresent()) {
            return optional.get();
        } else {
            ConversionResult conversionResult = new ConversionResult();
            conversionResult.setSourceMeasureId(key.getMeasureId());
            conversionResult.setStart(key.getStart());
            return conversionResult;
        }
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

        save(conversionResult);
    }

    public void addCqlConversionErrorMessage(ThreadSessionKey key, String error, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(key);

        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);
        libraryConversionResults.getCqlConversionResult().setResult(Boolean.FALSE);
        libraryConversionResults.getCqlConversionResult().getErrors().add(error);

        save(conversionResult);
    }


    public void addCql(ThreadSessionKey key, String cql, String name, String version, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(key);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);

        libraryConversionResults.setName(name);
        libraryConversionResults.setVersion(version);
        save(conversionResult);

        addLibraryData(key, cql, matLibraryId, LibraryType.QDM_CQL);


    }

    public void addFhirCql(ThreadSessionKey key, String cql, String matLibraryId) {
        addLibraryData(key, cql, matLibraryId, LibraryType.FHIR_CQL);
    }

    public void addFhirJson(ThreadSessionKey key, String json, String matLibraryId) {
        addLibraryData(key, json, matLibraryId, LibraryType.FHIR_ELM);
    }

    public void addExternalLibraryErrors(ThreadSessionKey key, Map<String, List<CqlConversionError>> map, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(key);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);
        libraryConversionResults.setExternalErrors(map);
        save(conversionResult);
    }

    public void addFhirLibraryId(ThreadSessionKey key, String fhirLibraryId, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(key);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);
        libraryConversionResults.setFhirLibraryId(fhirLibraryId);

        conversionResult.getLibraryMappings().put(matLibraryId, fhirLibraryId);

        save(conversionResult);
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

        libraryDataService.findOrCreate(conversionResult.getId(),
                conversionResult.getSourceMeasureId(),
                matLibraryId,
                type,
                data);

        save(conversionResult);
    }

    public String getCql(ThreadSessionKey key, String matLibraryId) {
        return getLibraryData(key, matLibraryId, LibraryType.QDM_CQL);
    }

    public String getElm(ThreadSessionKey key, String matLibraryId) {
        return getLibraryData(key, matLibraryId, LibraryType.QDM_ELM);
    }

    public String getFhirElm(ThreadSessionKey key, String matLibraryId) {
        return getLibraryData(key, matLibraryId, LibraryType.FHIR_ELM);
    }

    public String getFhirCql(ThreadSessionKey key, String matLibraryId) {
        return getLibraryData(key, matLibraryId, LibraryType.FHIR_CQL);
    }

    private String getLibraryData(ThreadSessionKey key, String matLibraryId, LibraryType type) {
        ConversionResult conversionResult = findOrCreate(key);

        var optional = libraryDataService.findByIndex(conversionResult.getId(),
                conversionResult.getSourceMeasureId(),
                matLibraryId,
                type);

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

        save(conversionResult);
    }

    public void addFhirCqlConversionErrors(ThreadSessionKey key, List<CqlConversionError> errors, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(key);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);
        libraryConversionResults.getCqlConversionResult().setResult(Boolean.FALSE);
        libraryConversionResults.getCqlConversionResult().getFhirCqlConversionErrors().addAll(errors);

        save(conversionResult);
    }

    public void addMatCqlConversionErrors(ThreadSessionKey key, List<MatCqlConversionException> errors, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(key);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);
        libraryConversionResults.getCqlConversionResult().setResult(Boolean.FALSE);
        libraryConversionResults.getCqlConversionResult().getMatCqlConversionErrors().addAll(errors);

        save(conversionResult);
    }

    public void addFhirMatCqlConversionErrors(ThreadSessionKey key, List<MatCqlConversionException> errors, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(key);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);
        libraryConversionResults.getCqlConversionResult().setResult(Boolean.FALSE);
        libraryConversionResults.getCqlConversionResult().getFhirMatCqlConversionErrors().addAll(errors);

        save(conversionResult);
    }

    public void addFhirMeasureValidationResults(ThreadSessionKey key,
                                                List<FhirValidationResult> list) {
        ConversionResult conversionResult = findOrCreate(key);

        findOrCreateMeasureConversionResults(conversionResult).setMeasureFhirValidationResults(list);

        save(conversionResult);
    }

    public void addLibraryValidationResults(ThreadSessionKey key,
                                            List<FhirValidationResult> list,
                                            String matLibraryId) {

        ConversionResult conversionResult = findOrCreate(key);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);

        libraryConversionResults.getLibraryFhirValidationResults().addAll(list);

        save(conversionResult);
    }

    public void addValueSetJson(ThreadSessionKey key, String oid, String json) {
        ConversionResult conversionResult = findOrCreate(key);

        ValueSetConversionResults valueSetConversionResults = conversionResult.findOrCreateValueSetConversionResults(oid);
        valueSetConversionResults.setJson(json);
        save(conversionResult);
    }

    public void addValueSetValidationResults(ThreadSessionKey key,
                                             String oid,
                                             List<FhirValidationResult> list) {
        ConversionResult conversionResult = findOrCreate(key);
        ValueSetConversionResults valueSetConversionResults = conversionResult.findOrCreateValueSetConversionResults(oid);
        valueSetConversionResults.getValueSetFhirValidationResults().addAll(list);

        save(conversionResult);
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

        save(conversionResult);
    }

    public void save(ConversionResult conversionResult) {
        conversionResultRepository.save(conversionResult);
    }

    public void addErrorMessage(ThreadSessionKey key, String message, ConversionOutcome outcome) {
        ConversionResult conversionResult = findOrCreate(key);
        conversionResult.setErrorReason(message);
        conversionResult.setOutcome(outcome);
        save(conversionResult);
    }

    public void addValueSetProcessingMemo(ThreadSessionKey key, String memo) {
        ConversionResult conversionResult = findOrCreate(key);
        conversionResult.setValueSetProcessingMemo(memo);
        conversionResult.setValueSetsProcessed(Instant.now());
        save(conversionResult);
    }

    public void addFhirMeasureJson(ThreadSessionKey key, String json) {
        ConversionResult conversionResult = findOrCreate(key);

        findOrCreateMeasureConversionResults(conversionResult).setFhirMeasureJson(json);

        save(conversionResult);
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

        save(conversionResult);
    }

    public void complete(ThreadSessionKey key) {
        ConversionResult conversionResult = findOrCreate(key);
        conversionResult.setFinished(Instant.now());
        save(conversionResult);
    }

    public void addConversionType(ThreadSessionKey key, ConversionType conversionType) {
        ConversionResult conversionResult = findOrCreate(key);
        conversionResult.setConversionType(conversionType);
        save(conversionResult);
    }

    public void addBatchId(ThreadSessionKey key, String batchId) {
        ConversionResult conversionResult = findOrCreate(key);
        conversionResult.setBatchId(batchId);
        save(conversionResult);
    }

    public void addXmlSource(ThreadSessionKey key, XmlSource xmlSource) {
        ConversionResult conversionResult = findOrCreate(key);
        conversionResult.setXmlSource(xmlSource);
        save(conversionResult);
    }

    public void addMeasureLibraryId(ThreadSessionKey key, String id) {
        ConversionResult conversionResult = findOrCreate(key);
        conversionResult.setFhirMeasureId(id);
        save(conversionResult);
    }

    public void addShowWarnings(ThreadSessionKey key, boolean flag) {
        ConversionResult conversionResult = findOrCreate(key);
        conversionResult.setShowWarnings(flag);
        save(conversionResult);
    }

    public void addVsacGrantingTicket(ThreadSessionKey key, String vsacGrantingTicket) {
        ConversionResult conversionResult = findOrCreate(key);
        conversionResult.setVsacGrantingTicket(vsacGrantingTicket);
        save(conversionResult);
    }


}
