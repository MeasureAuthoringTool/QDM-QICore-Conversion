package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.dto.*;
import gov.cms.mat.fhir.services.exceptions.LibraryConversionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ConversionResultsService {
    private final ConversionResultRepository conversionResultRepository;

    public ConversionResultsService(ConversionResultRepository conversionResultRepository) {
        this.conversionResultRepository = conversionResultRepository;
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

    void addLibraryFieldConversionResult(ThreadSessionKey key, FieldConversionResult result, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(key);

        LibraryConversionResults libraryConversionResult = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);

        libraryConversionResult.getLibraryResults().add(result);

        save(conversionResult);
    }

    public Optional<ConversionResult> findByMeasureId(ThreadSessionKey key) {
        return conversionResultRepository.findByMeasureIdAndStart(key.getMeasureId(), key.getStart());
    }

    public List<ConversionResult> findAll() {
        return conversionResultRepository.findAll(Sort.by(Sort.Direction.DESC, "modified"));
    }

    private ConversionResult findOrCreate(ThreadSessionKey key) {
        Optional<ConversionResult> optional = findByMeasureId(key);

        if (optional.isPresent()) {
            return optional.get();
        } else {
            ConversionResult conversionResult = new ConversionResult();
            conversionResult.setMeasureId(key.getMeasureId());
            conversionResult.setStart(key.getStart());
            return conversionResult;
        }
    }

    public ConversionResult findConversionResult(ThreadSessionKey key) {
        Optional<ConversionResult> optional = findByMeasureId(key);

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

    public void addCql(ThreadSessionKey key, String cql, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(key);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);

        libraryConversionResults.getCqlConversionResult().setCql(cql);

        save(conversionResult);
    }

    public void addElm(ThreadSessionKey key, String json, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(key);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);

        libraryConversionResults.getCqlConversionResult().setElm(json);

        save(conversionResult);
    }

    public void addCqlConversionErrors(ThreadSessionKey key, List<CqlConversionError> errors, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(key);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);
        libraryConversionResults.getCqlConversionResult().setResult(Boolean.FALSE);
        libraryConversionResults.getCqlConversionResult().getCqlConversionErrors().addAll(errors);

        save(conversionResult);
    }

    public void addMatCqlConversionErrors(ThreadSessionKey key, List<MatCqlConversionException> errors, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(key);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);
        libraryConversionResults.getCqlConversionResult().setResult(Boolean.FALSE);
        libraryConversionResults.getCqlConversionResult().getMatCqlConversionErrors().addAll(errors);

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
}
