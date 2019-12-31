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

    ConversionResult addValueSetResult(String measureId, ValueSetResult result) {
        ConversionResult conversionResult = getConversionResultWithValueSetConversionResults(measureId);

        conversionResult.getValueSetConversionResults().getValueSetResults().add(result);
        return conversionResultRepository.save(conversionResult);
    }

    ConversionResult addMeasureResult(String measureId, FieldConversionResult result) {
        ConversionResult conversionResult = findOrCreate(measureId);

        if (conversionResult.getMeasureConversionResults() == null) {
            conversionResult.setMeasureConversionResults(new MeasureConversionResults());
        }
        conversionResult.getMeasureConversionResults().getMeasureResults().add(result);
        return conversionResultRepository.save(conversionResult);
    }


    ConversionResult addLibraryResult(String measureId, FieldConversionResult result) {
        ConversionResult conversionResult = findOrCreate(measureId);

        if (conversionResult.getLibraryConversionResults() == null) {
            conversionResult.setLibraryConversionResults(new LibraryConversionResults());
        }

        conversionResult.getLibraryConversionResults().getLibraryResults().add(result);

        return conversionResultRepository.save(conversionResult);
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

            if (conversionResult.getValueSetConversionResults() != null) {
                conversionResult.getValueSetConversionResults().getValueSetResults().clear();
                conversionResult.getValueSetConversionResults().getValueSetFhirValidationErrors().clear();
                conversionResult.getValueSetConversionResults().setValueSetConversionType(null);
                conversionResultRepository.save(conversionResult);
            }
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
                conversionResult.getMeasureConversionResults().getMeasureFhirValidationErrors().clear();
                conversionResultRepository.save(conversionResult);
            }
        } else {
            log.trace("ConversionResult not found for measureId: {}", measureId);
        }
    }

    synchronized void clearMeasureOrchestration(String measureId) {
        ConversionResult conversionResult = findOrCreate(measureId);

        conversionResult.setValueSetConversionResults(new ValueSetConversionResults());
        conversionResult.setMeasureConversionResults(new MeasureConversionResults());
        conversionResult.setLibraryConversionResults(new LibraryConversionResults());
        conversionResult.setMeasureId(measureId);
        conversionResultRepository.save(conversionResult);
    }

    public ConversionResult findConversionResult(String measureId) {
        Optional<ConversionResult> optional = findByMeasureId(measureId);

        return optional
                .orElseThrow(() -> new LibraryConversionException("Cannot find ConversionResult for measureId: " + measureId));
    }

    public synchronized void setLibraryConversionType(String measureId, ConversionType conversionType) {
        ConversionResult conversionResult = findOrCreate(measureId);

        if (conversionResult.getLibraryConversionResults() == null) {
            conversionResult.setLibraryConversionResults(new LibraryConversionResults());
        }

        conversionResult.getLibraryConversionResults().setLibraryConversionType(conversionType);

        conversionResultRepository.save(conversionResult);
    }

    public synchronized void setValueSetConversionType(String measureId, ConversionType conversionType) {
        ConversionResult conversionResult = getConversionResultWithValueSetConversionResults(measureId);

        conversionResult.getValueSetConversionResults().setValueSetConversionType(conversionType);

        conversionResultRepository.save(conversionResult);
    }

    synchronized void clearLibrary(String measureId) {
        Optional<ConversionResult> optional = findByMeasureId(measureId);

        if (optional.isPresent()) {
            ConversionResult conversionResult = optional.get();

            if (conversionResult.getLibraryConversionResults() != null) {
                conversionResult.getLibraryConversionResults().getLibraryFhirValidationErrors().clear();
                conversionResult.getLibraryConversionResults().getLibraryResults().clear();
                conversionResult.getLibraryConversionResults().setLibraryConversionType(null);
                conversionResultRepository.save(conversionResult);
            }
        } else {
            log.trace("Not found by measureId: {}", measureId);
        }
    }

    synchronized void clearCqlConversionResult(String measureId) {
        Optional<ConversionResult> optional = findByMeasureId(measureId);

        if (optional.isPresent()) {
            ConversionResult conversionResult = optional.get();

            if (conversionResult.getLibraryConversionResults() != null && conversionResult.getLibraryConversionResults().getCqlConversionResult() != null) {
                conversionResult.getLibraryConversionResults().setCqlConversionResult(new CqlConversionResult());
            }

            conversionResultRepository.save(conversionResult);
        } else {
            log.trace("Not found by measureId: {}", measureId);
        }
    }

    public synchronized void addCqlConversionResultSuccess(String measureId) {
        ConversionResult conversionResult = findOrCreateCqlConversionResult(measureId);

        conversionResult.getLibraryConversionResults().getCqlConversionResult().setResult(Boolean.TRUE);

        conversionResultRepository.save(conversionResult);
    }

    public ConversionResult findOrCreateCqlConversionResult(String measureId) {
        ConversionResult conversionResult = findOrCreate(measureId);

        if (conversionResult.getLibraryConversionResults() == null) {
            conversionResult.setLibraryConversionResults(new LibraryConversionResults());
        }

        if (conversionResult.getLibraryConversionResults().getCqlConversionResult() == null) {
            conversionResult.getLibraryConversionResults().setCqlConversionResult(new CqlConversionResult());
        }
        return conversionResult;
    }

    public synchronized void addCqlConversionErrorMessage(String measureId, String error) {
        ConversionResult conversionResult = findOrCreateCqlConversionResult(measureId);

        conversionResult.getLibraryConversionResults().getCqlConversionResult().getErrors().add(error);
        conversionResult.getLibraryConversionResults().getCqlConversionResult().setResult(Boolean.FALSE);

        conversionResultRepository.save(conversionResult);
    }

    public synchronized void addCql(String measureId, String cql) {
        ConversionResult conversionResult = findOrCreateCqlConversionResult(measureId);

        conversionResult.getLibraryConversionResults().getCqlConversionResult().setCql(cql);

        conversionResultRepository.save(conversionResult);
    }

    public synchronized void addElm(String measureId, String json) {
        ConversionResult conversionResult = findOrCreateCqlConversionResult(measureId);

        conversionResult.getLibraryConversionResults().getCqlConversionResult().setElm(json);

        conversionResultRepository.save(conversionResult);
    }

    public synchronized void addCqlConversionErrors(String measureId, List<CqlConversionError> errors) {
        ConversionResult conversionResult = findOrCreateCqlConversionResult(measureId);
        conversionResult.getLibraryConversionResults().getCqlConversionResult().setCqlConversionErrors(errors);
        conversionResult.getLibraryConversionResults().getCqlConversionResult().setResult(Boolean.FALSE);
        conversionResultRepository.save(conversionResult);
    }

    public synchronized void addMatCqlConversionErrors(String measureId, List<MatCqlConversionException> errors) {
        ConversionResult conversionResult = findOrCreateCqlConversionResult(measureId);

        conversionResult.getLibraryConversionResults().getCqlConversionResult().getMatCqlConversionErrors().clear();
        conversionResult.getLibraryConversionResults().getCqlConversionResult().getMatCqlConversionErrors().addAll(errors);

        conversionResult.getLibraryConversionResults().getCqlConversionResult().setResult(Boolean.FALSE);
        conversionResultRepository.save(conversionResult);
    }

    public synchronized void setCqlConversionResult(String measureId, ConversionType conversionType) {
        ConversionResult conversionResult = findOrCreateCqlConversionResult(measureId);

        conversionResult.getLibraryConversionResults().getCqlConversionResult().setType(conversionType);

        conversionResultRepository.save(conversionResult);
    }

    public synchronized void setMeasureConversionType(String measureId, ConversionType conversionType) {
        ConversionResult conversionResult = findOrCreateCqlConversionResult(measureId);

        conversionResult.setMeasureId(measureId);

        if (conversionResult.getMeasureConversionResults() == null) {
            conversionResult.setMeasureConversionResults(new MeasureConversionResults());
        }
        conversionResult.getMeasureConversionResults().setMeasureConversionType(conversionType);

        conversionResultRepository.save(conversionResult);
    }

    public synchronized void addFhirMeasureValidationResults(String measureId,
                                                             List<FhirValidationResult> list) {
        ConversionResult conversionResult = findOrCreate(measureId);

        if (conversionResult.getMeasureConversionResults() == null) {
            conversionResult.setMeasureConversionResults(new MeasureConversionResults());
        }

        conversionResult.getMeasureConversionResults().setMeasureFhirValidationErrors(list);

        conversionResultRepository.save(conversionResult);
    }

    public synchronized void addLibraryValidationResults(String measureId,
                                                         List<FhirValidationResult> list) {

        ConversionResult conversionResult = findOrCreate(measureId);

        if (conversionResult.getLibraryConversionResults() == null) {
            conversionResult.setLibraryConversionResults(new LibraryConversionResults());
        }

        conversionResult.getLibraryConversionResults().setLibraryFhirValidationErrors(list);

        conversionResultRepository.save(conversionResult);
    }

    public synchronized void addValueSetValidationResults(String measureId, String oid,
                                                          List<FhirValidationResult> list) {
        ConversionResult conversionResult = getConversionResultWithValueSetConversionResults(measureId);

        ValueSetValidationResult validationResult = new ValueSetValidationResult(oid);
        validationResult.setLibraryFhirValidationErrors(list);
        conversionResult.getValueSetConversionResults().getValueSetFhirValidationErrors().add(validationResult);

        conversionResultRepository.save(conversionResult);
    }

    public ConversionResult getConversionResultWithValueSetConversionResults(String measureId) {
        ConversionResult conversionResult = findOrCreate(measureId);

        if (conversionResult.getValueSetConversionResults() == null) {
            conversionResult.setValueSetConversionResults(new ValueSetConversionResults());
        }
        return conversionResult;
    }
}
