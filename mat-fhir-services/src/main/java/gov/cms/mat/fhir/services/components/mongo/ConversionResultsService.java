package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.cql.MatCqlConversionException;
import gov.cms.mat.fhir.services.exceptions.LibraryConversionException;
import gov.cms.mat.fhir.services.service.support.CqlConversionError;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ConversionResultsService {
    private final ConversionResultRepository conversionResultRepository;

    public ConversionResultsService(ConversionResultRepository conversionResultRepository) {
        this.conversionResultRepository = conversionResultRepository;
    }

    ConversionResult addValueSetResult(String measureId, ConversionResult.ValueSetResult result) {
        ConversionResult conversionResult = findOrCreate(measureId);
        conversionResult.getValueSetResults().add(result);
        return conversionResultRepository.save(conversionResult);
    }

    ConversionResult addMeasureResult(String measureId, ConversionResult.FieldConversionResult result) {
        ConversionResult conversionResult = findOrCreate(measureId);
        conversionResult.getMeasureResults().add(result);
        return conversionResultRepository.save(conversionResult);
    }


    ConversionResult addLibraryResult(String measureId, ConversionResult.FieldConversionResult result) {
        ConversionResult conversionResult = findOrCreate(measureId);
        conversionResult.getLibraryResults().add(result);
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

    ConversionResult clearValueSetResults(String measureId) {
        Optional<ConversionResult> optional = findByMeasureId(measureId);

        if (optional.isPresent()) {
            ConversionResult conversionResult = optional.get();
            conversionResult.getValueSetResults().clear();
            conversionResult.getValueSetFhirValidationErrors().clear();
            conversionResult.setValueSetConversionType(null);
            return conversionResultRepository.save(conversionResult);
        } else {
            return null;
        }
    }

    synchronized ConversionResult clearMeasure(String measureId) {
        Optional<ConversionResult> optional = findByMeasureId(measureId);

        if (optional.isPresent()) {
            ConversionResult conversionResult = optional.get();
            conversionResult.getMeasureResults().clear();
            conversionResult.getMeasureFhirValidationErrors().clear();
            conversionResult.setMeasureConversionType(null);
            return conversionResultRepository.save(conversionResult);
        } else {
            return null;
        }
    }

    public ConversionResult findConversionResult(String measureId) {
        Optional<ConversionResult> optional = findByMeasureId(measureId);

        return optional
                .orElseThrow(() -> new LibraryConversionException("Cannot find ConversionResult for measureId: " + measureId));
    }

    public synchronized ConversionResult setLibraryConversionType(String measureId, ConversionType conversionType) {
        ConversionResult conversionResult = findOrCreate(measureId);

        conversionResult.setLibraryConversionType(conversionType);

        return conversionResultRepository.save(conversionResult);
    }

    public synchronized ConversionResult setValueSetConversionType(String measureId, ConversionType conversionType) {
        ConversionResult conversionResult = findOrCreate(measureId);

        conversionResult.setValueSetConversionType(conversionType);

        return conversionResultRepository.save(conversionResult);
    }

    synchronized ConversionResult clearLibrary(String measureId) {
        Optional<ConversionResult> optional = findByMeasureId(measureId);

        if (optional.isPresent()) {
            ConversionResult conversionResult = optional.get();
            conversionResult.getLibraryFhirValidationErrors().clear();
            conversionResult.getLibraryResults().clear();
            conversionResult.setLibraryConversionType(null);
            return conversionResultRepository.save(conversionResult);
        } else {
            return null;
        }
    }

    synchronized ConversionResult clearCqlConversionResult(String measureId) {
        Optional<ConversionResult> optional = findByMeasureId(measureId);

        if (optional.isPresent()) {
            ConversionResult conversionResult = optional.get();
            conversionResult.setCqlConversionResult(null);

            return conversionResultRepository.save(conversionResult);
        } else {
            return null;
        }
    }

    public synchronized ConversionResult addCqlConversionResultSuccess(String measureId) {
        ConversionResult conversionResult = findOrCreateCqlConversionResult(measureId);

        conversionResult.getCqlConversionResult().setResult(Boolean.TRUE);

        return conversionResultRepository.save(conversionResult);

    }

    public ConversionResult findOrCreateCqlConversionResult(String measureId) {
        ConversionResult conversionResult = findOrCreate(measureId);

        if (conversionResult.getCqlConversionResult() == null) {
            conversionResult.setCqlConversionResult(new ConversionResult.CqlConversionResult());
        }
        return conversionResult;
    }

    public synchronized ConversionResult addCqlConversionErrorMessage(String measureId, String error) {
        ConversionResult conversionResult = findOrCreateCqlConversionResult(measureId);

        if (conversionResult.getCqlConversionResult().getErrors() == null) {
            conversionResult.getCqlConversionResult().setErrors(new ArrayList<>());
        }

        conversionResult.getCqlConversionResult().getErrors().add(error);
        conversionResult.getCqlConversionResult().setResult(Boolean.FALSE);

        return conversionResultRepository.save(conversionResult);
    }

    public synchronized ConversionResult addCql(String measureId, String cql) {
        ConversionResult conversionResult = findOrCreateCqlConversionResult(measureId);

        conversionResult.getCqlConversionResult().setCql(cql);

        return conversionResultRepository.save(conversionResult);
    }

    public synchronized ConversionResult addElm(String measureId, String json) {
        ConversionResult conversionResult = findOrCreateCqlConversionResult(measureId);

        conversionResult.getCqlConversionResult().setElm(json);

        return conversionResultRepository.save(conversionResult);
    }

    public synchronized ConversionResult addCqlConversionErrors(String measureId, List<CqlConversionError> errors) {
        ConversionResult conversionResult = findOrCreateCqlConversionResult(measureId);
        conversionResult.getCqlConversionResult().setCqlConversionErrors(errors);
        conversionResult.getCqlConversionResult().setResult(Boolean.FALSE);
        return conversionResultRepository.save(conversionResult);
    }

    public synchronized ConversionResult addMatCqlConversionErrors(String measureId, List<MatCqlConversionException> errors) {
        ConversionResult conversionResult = findOrCreateCqlConversionResult(measureId);

        conversionResult.getCqlConversionResult().getMatCqlConversionErrors().clear();
        conversionResult.getCqlConversionResult().getMatCqlConversionErrors().addAll(errors);

        conversionResult.getCqlConversionResult().setResult(Boolean.FALSE);
        return conversionResultRepository.save(conversionResult);
    }

    public synchronized ConversionResult setCqlConversionResult(String measureId, ConversionType conversionType) {
        ConversionResult conversionResult = findOrCreateCqlConversionResult(measureId);

        conversionResult.getCqlConversionResult().setType(conversionType);

        return conversionResultRepository.save(conversionResult);
    }

    public synchronized ConversionResult setMeasureConversionType(String measureId, ConversionType conversionType) {
        ConversionResult conversionResult = findOrCreateCqlConversionResult(measureId);

        conversionResult.setMeasureId(measureId);
        conversionResult.setMeasureConversionType(conversionType);

        return conversionResultRepository.save(conversionResult);
    }

    public synchronized ConversionResult addFhirMeasureValidationResults(String measureId,
                                                                         List<ConversionResult.FhirValidationResult> list) {
        ConversionResult conversionResult = findOrCreate(measureId);

        conversionResult.setMeasureFhirValidationErrors(list);

        return conversionResultRepository.save(conversionResult);
    }

    public synchronized ConversionResult addLibraryValidationResults(String measureId,
                                                                     List<ConversionResult.FhirValidationResult> list) {

        ConversionResult conversionResult = findOrCreate(measureId);

        conversionResult.setLibraryFhirValidationErrors(list);

        return conversionResultRepository.save(conversionResult);
    }

    public synchronized ConversionResult addValueSetValidationResults(String measureId, String oid,
                                                                      List<ConversionResult.FhirValidationResult> list) {
        ConversionResult conversionResult = findOrCreate(measureId);

        ConversionResult.ValueSetValidationResult validationResult = new ConversionResult.ValueSetValidationResult(oid);
        validationResult.setLibraryFhirValidationErrors(list);

        conversionResult.getValueSetFhirValidationErrors().add(validationResult);

        return conversionResultRepository.save(conversionResult);
    }
}
