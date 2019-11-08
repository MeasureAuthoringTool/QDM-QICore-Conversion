package gov.cms.mat.fhir.services.components.mongo;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConversionResultsService {
    private final ConversionResultRepository conversionResultRepository;

    public ConversionResultsService(ConversionResultRepository conversionResultRepository) {
        this.conversionResultRepository = conversionResultRepository;
    }

    public ConversionResult addValueSetResult(String measureId, ConversionResult.ValueSetResult result) {
        ConversionResult conversionResult = findOrCreate(measureId);
        conversionResult.getValueSetResults().add(result);
        return conversionResultRepository.save(conversionResult);
    }

    public ConversionResult addMeasureResult(String measureId, ConversionResult.MeasureResult result) {
        ConversionResult conversionResult = findOrCreate(measureId);
        conversionResult.getMeasureResults().add(result);
        return conversionResultRepository.save(conversionResult);
    }

    public Optional<ConversionResult> findByMeasureId(String measureId) {
        return conversionResultRepository.findByMeasureId(measureId);
    }

    public ConversionResult findOrCreate(String measureId) {
        Optional<ConversionResult> optional = findByMeasureId(measureId);

        if (optional.isPresent()) {
            return optional.get();
        } else {
            ConversionResult conversionResult = new ConversionResult();
            conversionResult.setMeasureId(measureId);
            return conversionResultRepository.save(conversionResult);
        }
    }

    public ConversionResult clearValueSetResults(String measureId) {
        Optional<ConversionResult> optional = findByMeasureId(measureId);

        if (optional.isPresent()) {
            ConversionResult conversionResult = optional.get();
            conversionResult.getValueSetResults().clear();
            return conversionResultRepository.save(conversionResult);
        } else {
            return null;
        }
    }

    public ConversionResult clearMeasure(String measureId) {
        Optional<ConversionResult> optional = findByMeasureId(measureId);

        if (optional.isPresent()) {
            ConversionResult conversionResult = optional.get();
            conversionResult.getMeasureResults().clear();
            //todo could be much more to clear
            return conversionResultRepository.save(conversionResult);
        } else {
            return null;
        }
    }
}
