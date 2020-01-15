package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.dto.ConversionResultDto;
import gov.cms.mat.fhir.rest.dto.ValueSetConversionResults;
import gov.cms.mat.fhir.services.exceptions.ConversionResultsNotFoundException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ConversionResultProcessorService {
    private final ConversionResultsService conversionResultsService;

    public ConversionResultProcessorService(ConversionResultsService conversionResultsService) {
        this.conversionResultsService = conversionResultsService;
    }

    public List<ConversionResultDto> processAll() {
        return conversionResultsService.findAll()
                .stream()
                .map(this::buildDto)
                .collect(Collectors.toList());
    }

    public ConversionResultDto process(String measureId) {
        Optional<ConversionResult> optional = conversionResultsService.findByMeasureId(measureId);

        if (optional.isPresent()) {
            return buildDto(optional.get());
        } else {
            throw new ConversionResultsNotFoundException(measureId);
        }
    }

    private ConversionResultDto buildDto(ConversionResult conversionResult) {
        return ConversionResultDto.builder()
                .measureId(conversionResult.getMeasureId())
                .modified(conversionResult.getModified() == null ? null : conversionResult.getModified().toString())
                .valueSetConversionResults(conversionResult.getValueSetConversionResults())
                .measureConversionResults(conversionResult.getMeasureConversionResults())
                .libraryConversionResults(conversionResult.getLibraryConversionResults())
                .errorReason(conversionResult.getErrorReason())
                .outcome(conversionResult.getOutcome())
                .conversionType(conversionResult.getConversionType())
                .build();
    }

    public Set<String> findMissingValueSets() {
        return conversionResultsService.findAll()
                .stream()
                .filter(this::hasData)
                .map(ConversionResult::getValueSetConversionResults)
                .flatMap(List::stream)
                .filter(v -> !v.getSuccess())
                .map(ValueSetConversionResults::getOid)
                .collect(Collectors.toSet());
    }

    private boolean hasData(ConversionResult c) {
        return c.getValueSetConversionResults() != null &&
                CollectionUtils.isNotEmpty(c.getValueSetConversionResults());
    }
}