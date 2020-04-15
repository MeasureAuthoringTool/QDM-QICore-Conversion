package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.rest.dto.CqlConversionError;
import gov.cms.mat.fhir.rest.dto.CqlConversionReportError;
import gov.cms.mat.fhir.rest.dto.CqlConversionResult;
import gov.cms.mat.fhir.rest.dto.LibraryConversionResults;
import gov.cms.mat.fhir.services.components.mongo.ConversionResult;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.exceptions.BatchIdNotFoundException;
import groovy.util.logging.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LibraryErrorReportService {
    private final ConversionResultsService conversionResultsService;

    public LibraryErrorReportService(ConversionResultsService conversionResultsService) {
        this.conversionResultsService = conversionResultsService;
    }

    public List<CqlConversionReportError> processCountForBatch(String batchId) {
        List<CqlConversionReportError> cqlConversionReportErrors = processAllForBatch(batchId);

        List<CqlConversionReportError> sorted = new ArrayList<>();

        cqlConversionReportErrors.forEach(c -> processSortedBatch(c, sorted));

        return sorted.stream()
                .sorted(Comparator.comparingInt(CqlConversionReportError::getCount))
                .collect(Collectors.toList());
    }

    private void processSortedBatch(CqlConversionReportError c, List<CqlConversionReportError> sorted) {
        int index = sorted.indexOf(c);

        if (index > -1) {
            CqlConversionReportError existed = sorted.get(index);

            existed.setCount(existed.getCount() + 1);
        } else {
            sorted.add(c);
        }
    }


    public List<CqlConversionReportError> processAllForBatch(String batchId) {
        if (conversionResultsService.checkBatchIdUsed(batchId)) {
            return processBatchId(batchId);
        } else {
            throw new BatchIdNotFoundException(batchId);
        }
    }

    private List<CqlConversionReportError> processBatchId(String batchId) {
        List<ConversionResult> results = conversionResultsService.findByBatchId(batchId);

        return results.stream()
                .filter(this::checkConversionResult)
                .map(this::convert)
                .map(this::convertToReportList)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<CqlConversionReportError> convertToReportList(List<CqlConversionResult> cqlConversionResults) {
        return cqlConversionResults.stream()
                //   .filter(l -> CollectionUtils.isNotEmpty(l.getCqlConversionErrors()))
                .map(CqlConversionResult::getFhirCqlConversionErrors)
                .flatMap(Set::stream)
                .map(this::convertToReport)
                .collect(Collectors.toList());
    }

    private CqlConversionReportError convertToReport(CqlConversionError l) {
        CqlConversionReportError cqlConversionReportError = CqlConversionReportError.builder()
                .errorSeverity(l.getErrorSeverity())
                .libraryId(l.getTargetIncludeLibraryId())
                .libraryVersion(l.getTargetIncludeLibraryVersionId())
                .count(1)
                .build();

        cqlConversionReportError.setStartLine(l.getStartLine());
        cqlConversionReportError.setStartChar(l.getStartChar());
        cqlConversionReportError.setEndLine(l.getEndLine());
        cqlConversionReportError.setEndChar(l.getEndChar());
        cqlConversionReportError.setErrorType(l.getErrorType());

        return cqlConversionReportError;
    }

    private List<CqlConversionResult> convert(ConversionResult conversionResult) {
        return conversionResult.getLibraryConversionResults().stream()
                .map(LibraryConversionResults::getCqlConversionResult)
                .collect(Collectors.toList());
    }


    private boolean checkConversionResult(ConversionResult result) {
        return CollectionUtils.isNotEmpty(result.getLibraryConversionResults());
    }
}
