package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.dto.ConversionResultDto;
import gov.cms.mat.fhir.rest.dto.ValueSetConversionResults;
import gov.cms.mat.fhir.services.exceptions.BatchIdNotFoundException;
import gov.cms.mat.fhir.services.exceptions.ConversionResultsNotFoundException;
import gov.cms.mat.fhir.services.exceptions.ConversionResultsTooLargeException;
import gov.cms.mat.fhir.services.rest.TranslationReportController;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static gov.cms.mat.fhir.services.rest.TranslationReportController.DocumentsToFind.ALL;

@Service
public class ConversionResultProcessorService {
    private static final int MAX_RECORDS = 100;
    private final ConversionResultsService conversionResultsService;

    public ConversionResultProcessorService(ConversionResultsService conversionResultsService) {
        this.conversionResultsService = conversionResultsService;
    }

    public List<ConversionResultDto> processAllForBatch(String batchId) {
        if (conversionResultsService.checkBatchIdUsed(batchId)) {
            return processBatchId(batchId);
        } else {
            throw new BatchIdNotFoundException(batchId);
        }
    }

    private List<ConversionResultDto> processBatchId(String batchId) {
        List<ConversionResult> results = conversionResultsService.findByBatchId(batchId);

        if (results.size() > MAX_RECORDS) {
            throw new ConversionResultsTooLargeException(MAX_RECORDS, results.size());
        } else {
            return convertToDto(conversionResultsService.findByBatchId(batchId));
        }
    }

    private List<ConversionResultDto> convertToDto(List<ConversionResult> results) {
        return results.stream()
                .map(this::buildDto)
                .collect(Collectors.toList());
    }


    public ConversionResultDto process(ThreadSessionKey key) {
        Optional<ConversionResult> optional = conversionResultsService.findByMeasureId(key);

        if (optional.isPresent()) {
            return buildDto(optional.get());
        } else {
            throw new ConversionResultsNotFoundException(key);
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


    public Set<String> findMissingValueSets(String batchId) {
        if (conversionResultsService.checkBatchIdUsed(batchId)) {
            return processMissingValueSets(batchId);
        } else {
            throw new BatchIdNotFoundException(batchId);
        }
    }

    private Set<String> processMissingValueSets(String batchId) {
        return conversionResultsService.findByBatchId(batchId)
                .stream()
                .filter(this::hasData)
                .map(ConversionResult::getValueSetConversionResults)
                .flatMap(List::stream)
                .filter(v -> v.getSuccess() != null && !v.getSuccess())
                .map(ValueSetConversionResults::getOid)
                .collect(Collectors.toSet());
    }


    private boolean hasData(ConversionResult c) {
        return c.getValueSetConversionResults() != null &&
                CollectionUtils.isNotEmpty(c.getValueSetConversionResults());
    }

    public List<ConversionResultDto> processOne(String measureId, TranslationReportController.DocumentsToFind find) {
        if (find.equals(ALL)) {
            return convertToDto(conversionResultsService.findAllByMeasureId(measureId));
        } else {
            return processTop(measureId);
        }
    }

    private List<ConversionResultDto> processTop(String measureId) {
        Optional<ConversionResult> optional = conversionResultsService.findTopByMeasureId(measureId);

        if (optional.isPresent()) {
            return convertToDto(Collections.singletonList(optional.get()));
        } else {
            return Collections.emptyList();
        }
    }
}