package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.dto.ConversionOutcome;
import gov.cms.mat.fhir.rest.dto.ConversionResultDto;
import gov.cms.mat.fhir.rest.dto.CqlConversionError;
import gov.cms.mat.fhir.rest.dto.CqlConversionResult;
import gov.cms.mat.fhir.rest.dto.FhirValidationResult;
import gov.cms.mat.fhir.rest.dto.LibraryConversionResults;
import gov.cms.mat.fhir.rest.dto.ValueSetConversionResults;
import gov.cms.mat.fhir.services.exceptions.BatchIdNotFoundException;
import gov.cms.mat.fhir.services.exceptions.ConversionResultsNotFoundException;
import gov.cms.mat.fhir.services.exceptions.ConversionResultsTooLargeException;
import gov.cms.mat.fhir.services.rest.TranslationReportController;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static gov.cms.mat.fhir.services.rest.TranslationReportController.DocumentsToFind.ALL;

@Service
@Slf4j
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
            return convertToDto(results);
        }
    }

    private List<ConversionResultDto> convertToDto(List<ConversionResult> results) {
        return results.stream()
                .map(this::buildDto)
                .collect(Collectors.toList());
    }


    public ConversionResultDto process(ThreadSessionKey key) {
        Optional<ConversionResult> optional = conversionResultsService.findByThreadSessionKey(key);

        if (optional.isPresent()) {
            return buildDto(optional.get());
        } else {
            throw new ConversionResultsNotFoundException(key);
        }
    }

    public ConversionResultDto processLibrary(ThreadSessionKey key) {
        Optional<ConversionResult> optional = conversionResultsService.findByThreadSessionKey(key);
        if (optional.isPresent()) {

            ConversionResult c = optional.get();
            List<FhirValidationResult> errorsForAllLibs = new ArrayList<>();
            List<CqlConversionError> cqlConversionErrors = new ArrayList<>();

            c.libraryConversionResults.stream().forEach(r -> {
                if (CollectionUtils.isNotEmpty(r.getLibraryFhirValidationResults())) {
                    errorsForAllLibs.addAll(r.getLibraryFhirValidationResults());
                }
                if (MapUtils.isNotEmpty(r.getExternalErrors())) {
                    r.getExternalErrors().forEach((k,v) -> {
                        if (CollectionUtils.isNotEmpty(v)) {
                            cqlConversionErrors.addAll(v);
                        }
                    });
                }
            });

            boolean conversionErrors = cqlConversionErrors.stream().anyMatch(e -> StringUtils.equalsIgnoreCase("Error",e.getErrorSeverity()));
            boolean hasLibErrors = errorsForAllLibs.stream().anyMatch(r -> StringUtils.equalsIgnoreCase("Error",r.getSeverity()));
            c.setOutcome(conversionErrors || hasLibErrors ? ConversionOutcome.SUCCESS_WITH_ERROR : ConversionOutcome.SUCCESS);
            return buildDto(c);
        } else {
            throw new ConversionResultsNotFoundException(key);
        }
    }



    private ConversionResultDto buildDto(ConversionResult conversionResult) {
        conversionResult.getLibraryConversionResults().forEach(this::addLibraryData);

        ConversionOutcome outcome = conversionResult.getOutcome();

        return ConversionResultDto.builder()
                .measureId(conversionResult.getSourceMeasureId())
                .modified(conversionResult.getModified() == null ? null : conversionResult.getModified().toString())
                .valueSetConversionResults(conversionResult.getValueSetConversionResults())
                .measureConversionResults(conversionResult.getMeasureConversionResults())
                .libraryConversionResults(conversionResult.getLibraryConversionResults())
                .errorReason(conversionResult.getErrorReason())
                .outcome(outcome)
                .conversionType(conversionResult.getConversionType())
                .build();
    }

    private void addLibraryData(LibraryConversionResults libraryConversionResults) {
        if (libraryConversionResults.getCqlConversionResult() != null) {
            CqlConversionResult cqlConversionResult = libraryConversionResults.getCqlConversionResult();
            cqlConversionResult.setCql(ConversionReporter.getCql(libraryConversionResults.getMatLibraryId()));
            cqlConversionResult.setElm(ConversionReporter.getElm(libraryConversionResults.getMatLibraryId()));

            cqlConversionResult.setFhirCql(ConversionReporter.getFhirCql(libraryConversionResults.getMatLibraryId()));
            cqlConversionResult.setFhirElm(ConversionReporter.getFhirElmJson(libraryConversionResults.getMatLibraryId()));
        }
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
            return convertToDto(conversionResultsService.findAllBySourceMeasureId(measureId));
        } else {
            return processTop(measureId);
        }
    }

    private List<ConversionResultDto> processTop(String measureId) {
        Optional<ConversionResult> optional = conversionResultsService.findTopBySourceMeasureId(measureId);

        if (optional.isPresent()) {
            return convertToDto(Collections.singletonList(optional.get()));
        } else {
            return Collections.emptyList();
        }
    }
}