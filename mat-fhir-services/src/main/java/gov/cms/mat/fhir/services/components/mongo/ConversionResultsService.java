package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.dto.*;
import gov.cms.mat.fhir.services.exceptions.LibraryConversionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ConversionResultsService {
    private final ConversionResultRepository conversionResultRepository;

    public ConversionResultsService(ConversionResultRepository conversionResultRepository) {
        this.conversionResultRepository = conversionResultRepository;
    }

    void addValueSetResult(String measureId,
                           String oid,
                           String reason,
                           Boolean success,
                           String link) {
        ConversionResult conversionResult = findOrCreate(measureId);

        ValueSetConversionResults valueSetConversionResults = conversionResult.findOrCreateValueSetConversionResults(oid);
        valueSetConversionResults.setSuccess(success);
        valueSetConversionResults.setReason(reason);
        valueSetConversionResults.setLink(link);


        save(conversionResult);
    }


    void addMeasureResult(String measureId, FieldConversionResult result) {
        ConversionResult conversionResult = findOrCreate(measureId);

        if (conversionResult.getMeasureConversionResults() == null) {
            conversionResult.setMeasureConversionResults(new MeasureConversionResults());
        }
        conversionResult.getMeasureConversionResults().getMeasureResults().add(result);
        save(conversionResult);
    }

    void addLibraryFieldConversionResult(String measureId, FieldConversionResult result, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(measureId);

        LibraryConversionResults libraryConversionResult = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);

        libraryConversionResult.getLibraryResults().add(result);

        save(conversionResult);
    }

    public Optional<ConversionResult> findByMeasureId(String measureId) {
        return conversionResultRepository.findByMeasureId(measureId);
    }

    public List<ConversionResult> findAll() {
        return conversionResultRepository.findAll(Sort.by(Sort.Direction.DESC, "modified"));
    }

    private synchronized ConversionResult findOrCreate(String measureId) {
        Optional<ConversionResult> optional = findByMeasureId(measureId);

        if (optional.isPresent()) {
            return optional.get();
        } else {
            ConversionResult conversionResult = new ConversionResult();
            conversionResult.setMeasureId(measureId);
            return conversionResult;
        }
    }

    void clearValueSetResults(String measureId) {
        Optional<ConversionResult> optional = findByMeasureId(measureId);

        if (optional.isPresent()) {
            ConversionResult conversionResult = optional.get();
            conversionResult.getValueSetConversionResults().clear();
            conversionResultRepository.save(conversionResult);
        } else {
            log.trace("ConversionResult not found for measureId: {}", measureId);
        }
    }

    synchronized void clearMeasure(String measureId) {
        Optional<ConversionResult> optional = findByMeasureId(measureId);

        if (optional.isPresent()) {
            ConversionResult conversionResult = optional.get();

            if (conversionResult.getMeasureConversionResults() != null) {
                conversionResult.getMeasureConversionResults().getMeasureResults().clear();
                conversionResult.getMeasureConversionResults().setMeasureConversionType(null);
                conversionResult.getMeasureConversionResults().getMeasureFhirValidationResults().clear();
                save(conversionResult);
            }
        } else {
            log.trace("ConversionResult not found for measureId: {}", measureId);
        }
    }

    synchronized void clearMeasureOrchestration(String measureId) {
        ConversionResult conversionResult = findOrCreate(measureId);

        conversionResult.getValueSetConversionResults().clear();

        conversionResult.setMeasureConversionResults(new MeasureConversionResults());
        conversionResult.getLibraryConversionResults().clear();
        conversionResult.setMeasureId(measureId);

        conversionResult.setErrorReason(null);

        save(conversionResult);
    }

    public ConversionResult findConversionResult(String measureId) {
        Optional<ConversionResult> optional = findByMeasureId(measureId);

        return optional
                .orElseThrow(() ->
                        new LibraryConversionException("Cannot find ConversionResult for measureId: " + measureId));
    }

    public synchronized void setLibraryConversionType(String measureId,
                                                      ConversionType conversionType) {
        ConversionResult conversionResult = findOrCreate(measureId);
        conversionResult.setConversionType(conversionType);

        save(conversionResult);
    }


    synchronized void clearLibrary(String measureId) {
        Optional<ConversionResult> optional = findByMeasureId(measureId);

        if (optional.isPresent()) {
            ConversionResult conversionResult = optional.get();
            conversionResult.getLibraryConversionResults().clear();

            conversionResultRepository.save(conversionResult);
        } else {
            log.trace("Not found by measureId: {}", measureId);
        }
    }


    public synchronized void addCqlConversionResultSuccess(String measureId, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(measureId);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);
        libraryConversionResults.getCqlConversionResult().setResult(Boolean.TRUE);


        save(conversionResult);
    }


    public synchronized void addCqlConversionErrorMessage(String measureId, String error, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(measureId);

        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);
        libraryConversionResults.getCqlConversionResult().setResult(Boolean.FALSE);
        libraryConversionResults.getCqlConversionResult().getErrors().add(error);

        save(conversionResult);
    }

    public synchronized void addCql(String measureId, String cql, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(measureId);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);

        libraryConversionResults.getCqlConversionResult().setCql(cql);

        save(conversionResult);
    }

    public synchronized void addElm(String measureId, String json, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(measureId);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);

        libraryConversionResults.getCqlConversionResult().setElm(json);

        save(conversionResult);
    }

    public synchronized void addCqlConversionErrors(String measureId, List<CqlConversionError> errors, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(measureId);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);
        libraryConversionResults.getCqlConversionResult().setResult(Boolean.FALSE);
        libraryConversionResults.getCqlConversionResult().getCqlConversionErrors().addAll(errors);

        save(conversionResult);
    }

    public synchronized void addMatCqlConversionErrors(String measureId, List<MatCqlConversionException> errors, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(measureId);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);
        libraryConversionResults.getCqlConversionResult().setResult(Boolean.FALSE);
        libraryConversionResults.getCqlConversionResult().getMatCqlConversionErrors().addAll(errors);

        save(conversionResult);
    }


    public synchronized void setMeasureConversionType(String measureId, ConversionType conversionType) {
        ConversionResult conversionResult = findOrCreate(measureId);


        //todo remove

        conversionResult.setMeasureId(measureId);

        if (conversionResult.getMeasureConversionResults() == null) {
            conversionResult.setMeasureConversionResults(new MeasureConversionResults());
        }
        conversionResult.getMeasureConversionResults().setMeasureConversionType(conversionType);

        save(conversionResult);
    }

    public synchronized void addFhirMeasureValidationResults(String measureId,
                                                             List<FhirValidationResult> list) {
        ConversionResult conversionResult = findOrCreate(measureId);

        if (conversionResult.getMeasureConversionResults() == null) {
            conversionResult.setMeasureConversionResults(new MeasureConversionResults());
        }

        conversionResult.getMeasureConversionResults().setMeasureFhirValidationResults(list);

        save(conversionResult);
    }

    public synchronized void addLibraryValidationResults(String measureId,
                                                         List<FhirValidationResult> list,
                                                         String matLibraryId) {

        ConversionResult conversionResult = findOrCreate(measureId);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);

        libraryConversionResults.getLibraryFhirValidationResults().addAll(list);

        save(conversionResult);
    }

    public synchronized void addValueSetValidationResults(String measureId,
                                                          String oid,
                                                          List<FhirValidationResult> list) {
        ConversionResult conversionResult = findOrCreate(measureId);
        ValueSetConversionResults valueSetConversionResults = conversionResult.findOrCreateValueSetConversionResults(oid);
        valueSetConversionResults.getValueSetFhirValidationResults().addAll(list);

        save(conversionResult);
    }


    public synchronized void addLibraryConversionResult(String measureId,
                                                        String link,
                                                        String message,
                                                        Boolean success,
                                                        String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(measureId);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);

        libraryConversionResults.setLink(link);
        libraryConversionResults.setReason(message);
        libraryConversionResults.setSuccess(success);

        save(conversionResult);
    }

    public synchronized void save(ConversionResult conversionResult) {
        conversionResultRepository.save(conversionResult);
    }

    public void addErrorMessage(String measureId, String message) {
        ConversionResult conversionResult = findOrCreate(measureId);
        conversionResult.setErrorReason(message);
        save(conversionResult);
    }

    public void addFhirMeasureJson(String measureId, String json) {
        ConversionResult conversionResult = findOrCreate(measureId);
        conversionResult.setFhirMeasureJson(json);
        save(conversionResult);
    }
}
