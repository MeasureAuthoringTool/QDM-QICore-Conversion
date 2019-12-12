package gov.cms.mat.fhir.services.components.mongo;

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

    ConversionResult addMeasureResult(String measureId, ConversionResult.MeasureResult result) {
        ConversionResult conversionResult = findOrCreate(measureId);
        conversionResult.getMeasureResults().add(result);
        return conversionResultRepository.save(conversionResult);
    }

    ConversionResult addLibraryResult(String measureId, ConversionResult.LibraryResult result) {
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
            return conversionResultRepository.save(conversionResult);
        } else {
            return null;
        }
    }

    ConversionResult clearMeasure(String measureId) {
        Optional<ConversionResult> optional = findByMeasureId(measureId);

        if (optional.isPresent()) {
            ConversionResult conversionResult = optional.get();
            conversionResult.getMeasureResults().clear();
            return conversionResultRepository.save(conversionResult);
        } else {
            return null;
        }
    }

    ConversionResult clearLibrary(String measureId) {
        Optional<ConversionResult> optional = findByMeasureId(measureId);

        if (optional.isPresent()) {
            ConversionResult conversionResult = optional.get();
            conversionResult.getLibraryResults().clear();
            return conversionResultRepository.save(conversionResult);
        } else {
            return null;
        }
    }

    ConversionResult clearCqlConversionResult(String measureId) {
        Optional<ConversionResult> optional = findByMeasureId(measureId);

        if (optional.isPresent()) {
            ConversionResult conversionResult = optional.get();

            conversionResult.setCqlConversionResult(null);

            return conversionResultRepository.save(conversionResult);
        } else {
            return null;
        }
    }

    public ConversionResult addCqlConversionResultSuccess(String measureId) {
        ConversionResult conversionResult = findOrCreate(measureId);

        if (conversionResult.getCqlConversionResult() == null) {
            conversionResult.setCqlConversionResult(new ConversionResult.CqlConversionResult());
        }

        conversionResult.getCqlConversionResult().setResult(Boolean.TRUE);

        return conversionResultRepository.save(conversionResult);

    }

    public ConversionResult addCqlConversionError(String measureId, String error) {
        ConversionResult conversionResult = findOrCreate(measureId);

        if (conversionResult.getCqlConversionResult() == null) {
            conversionResult.setCqlConversionResult(new ConversionResult.CqlConversionResult());
        }

        if (conversionResult.getCqlConversionResult().getErrors() == null) {
            conversionResult.getCqlConversionResult().setErrors(new ArrayList<>());
        }

        conversionResult.getCqlConversionResult().getErrors().add(error);
        conversionResult.getCqlConversionResult().setResult(Boolean.FALSE);

        return conversionResultRepository.save(conversionResult);
    }
}
