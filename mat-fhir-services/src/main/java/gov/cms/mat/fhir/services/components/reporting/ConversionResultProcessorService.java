package gov.cms.mat.fhir.services.components.reporting;

import gov.cms.mat.fhir.rest.dto.ConversionOutcome;
import gov.cms.mat.fhir.rest.dto.ConversionResultDto;
import gov.cms.mat.fhir.rest.dto.CqlConversionError;
import gov.cms.mat.fhir.rest.dto.CqlConversionResult;
import gov.cms.mat.fhir.rest.dto.FhirValidationResult;
import gov.cms.mat.fhir.rest.dto.LibraryConversionResults;
import gov.cms.mat.fhir.services.exceptions.ConversionResultsNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
public class ConversionResultProcessorService {

    private final ConversionResultsService conversionResultsService;

    public ConversionResultProcessorService(ConversionResultsService conversionResultsService) {
        this.conversionResultsService = conversionResultsService;
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
            return getConversionResultDto(optional.get());
        } else {
            throw new ConversionResultsNotFoundException(key);
        }
    }

    private ConversionResultDto getConversionResultDto(ConversionResult c) {
        List<FhirValidationResult> errorsForAllLibs = new ArrayList<>();
        List<CqlConversionError> cqlConversionErrors = new ArrayList<>();

        c.libraryConversionResults.forEach(r -> {
            if (CollectionUtils.isNotEmpty(r.getLibraryFhirValidationResults())) {
                errorsForAllLibs.addAll(r.getLibraryFhirValidationResults());
            }
            if (MapUtils.isNotEmpty(r.getExternalErrors())) {
                r.getExternalErrors().forEach((k, v) -> {
                    if (CollectionUtils.isNotEmpty(v)) {
                        cqlConversionErrors.addAll(v);
                    }
                });
            }
        });

        boolean conversionErrors = cqlConversionErrors.stream().anyMatch(e -> StringUtils.equalsIgnoreCase("Error", e.getErrorSeverity()));
        boolean hasLibErrors = errorsForAllLibs.stream().anyMatch(r -> StringUtils.equalsIgnoreCase("Error", r.getSeverity()));
        c.setOutcome(conversionErrors || hasLibErrors ? ConversionOutcome.SUCCESS_WITH_ERROR : ConversionOutcome.SUCCESS);
        return buildDto(c);
    }

    private ConversionResultDto buildDto(ConversionResult conversionResult) {
        conversionResult.getLibraryConversionResults().forEach(this::addLibraryData);

        ConversionOutcome outcome = conversionResult.getOutcome();

        return ConversionResultDto.builder()
                .measureId(conversionResult.getSourceMeasureId())
                .modified(conversionResult.getModified() == null ? null : conversionResult.getModified().toString())
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
}