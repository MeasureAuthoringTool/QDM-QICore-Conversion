package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.services.exceptions.ConversionResultsNotFound;
import gov.cms.mat.fhir.services.service.QdmQiCoreDataService;
import gov.cms.mat.fhir.services.service.support.ConversionMapping;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConversionResultService {
    private final QdmQiCoreDataService qdmQiCoreDataService;
    private final ConversionResultsService conversionResultsService;

    public ConversionResultService(QdmQiCoreDataService qdmQiCoreDataService,
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

    public ConversionResultDto processSearchData(String measureId) {
        Optional<ConversionResult> optional = conversionResultsService.findByMeasureId(measureId);

        if (optional.isPresent()) {
            return buildDto(optional.get());
        } else {
            throw new ConversionResultsNotFound(measureId);
        }
    }

    private ConversionResultDto buildDto(ConversionResult conversionResult) {
        return ConversionResultDto.builder()
                .measureId(conversionResult.getMeasureId())
                .libraryResults(conversionResult.getLibraryResults())
                .valueSetResults(conversionResult.getValueSetResults())
                .measureResultMappingDtos(processMeasureResults(conversionResult))
                .build();
    }

    private List<MeasureResultMappingDto> processMeasureResults(ConversionResult conversionResult) {
        return conversionResult.getMeasureResults()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private MeasureResultMappingDto convertToDto(ConversionResult.MeasureResult measureResult) {
        try {
            ConversionMapping conversionMapping =
                    qdmQiCoreDataService.findByFhirR4QiCoreMapping(measureResult.field, measureResult.destination);

            return new MeasureResultMappingDto(measureResult, conversionMapping);
        } catch (Exception e) {
            return new MeasureResultMappingDto(measureResult, e.getMessage());
        }
    }
}