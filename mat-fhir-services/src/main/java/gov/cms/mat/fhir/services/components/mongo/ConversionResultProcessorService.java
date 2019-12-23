package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.cql.ValueSetResult;
import gov.cms.mat.fhir.services.exceptions.ConversionResultsNotFoundException;
import gov.cms.mat.fhir.services.service.QdmQiCoreDataService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ConversionResultProcessorService {
    private final QdmQiCoreDataService qdmQiCoreDataService;
    private final ConversionResultsService conversionResultsService;

    public ConversionResultProcessorService(QdmQiCoreDataService qdmQiCoreDataService,
                                            ConversionResultsService conversionResultsService) {
        this.qdmQiCoreDataService = qdmQiCoreDataService;
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
                .modified(conversionResult.getModified())
                .valueSetConversionResults(conversionResult.getValueSetConversionResults())
                .measureConversionResults(conversionResult.getMeasureConversionResults())
                .libraryConversionResults(conversionResult.getLibraryConversionResults())
                //.valueSetResults(conversionResult.getValueSetResults())
                //.valueSetConversionType(conversionResult.getValueSetConversionType())
                //.valueSetFhirValidationErrors(conversionResult.getValueSetFhirValidationErrors())
                //.measureResults(processMeasureResults(conversionResult))
                // .measureConversionType(conversionResult.getMeasureConversionType())
                // .measureFhirValidationErrors(conversionResult.getMeasureFhirValidationErrors())
//                .libraryResults(conversionResult.getLibraryResults())
//                .libraryConversionType(conversionResult.getLibraryConversionType())
//                .libraryFhirValidationErrors(conversionResult.getLibraryFhirValidationErrors())
                .cqlConversionResult(conversionResult.getCqlConversionResult())
                .build();
    }

//    private List<MeasureResultMappingDto> processMeasureResults(ConversionResult conversionResult) {
//        return conversionResult.getMeasureResults()
//                .stream()
//                .map(this::convertToDto)
//                .collect(Collectors.toList());
//    }

//    private MeasureResultMappingDto convertToDto(FieldConversionResult measureResult) {
//        try {
//            ConversionMapping conversionMapping =
//                    qdmQiCoreDataService.findByFhirR4QiCoreMapping(measureResult.getField(), measureResult.getDestination());
//
//            return new MeasureResultMappingDto(measureResult, conversionMapping);
//        } catch (Exception e) {
//            return new MeasureResultMappingDto(measureResult, e.getMessage());
//        }
//    }

    public Set<String> findMissingValueSets() {
        return conversionResultsService.findAll()
                .stream()
                .filter(this::hasData)
                .map(c -> c.getValueSetConversionResults().getValueSetResults())
                .flatMap(List::stream)
                .map(ValueSetResult::getOid)
                .collect(Collectors.toSet());
    }

    private boolean hasData(ConversionResult c) {
        return c.getValueSetConversionResults() != null &&
                CollectionUtils.isNotEmpty(c.getValueSetConversionResults().getValueSetResults());
    }

    public List<ValueSetResult> getValueSetResults(ConversionResult c) {
        return c.getValueSetConversionResults().getValueSetResults();
    }
}