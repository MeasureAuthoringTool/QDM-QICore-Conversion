package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.rest.dto.ConversionResultDto;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultProcessorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(path = "/report")
@Tag(name = "TranslationReport-Controller", description = "API for for getting conversion results.")
@Slf4j
public class TranslationReportController {
    private final ConversionResultProcessorService conversionResultProcessorService;

    public TranslationReportController(ConversionResultProcessorService conversionResultProcessorService) {
        this.conversionResultProcessorService = conversionResultProcessorService;
    }

    @Operation(summary = "Display all missing ValueSets.",
            description = "Display the OID's of the ValueSets that could not be located in VSAC.")
    @GetMapping(path = "/missingValueSets")
    public Set<String> findMissingValueSets(@RequestParam String batchId) {
        return conversionResultProcessorService.findMissingValueSets(batchId);
    }

    @Operation(summary = "Find report for a measure.",
            description = "Find and return a error report for a measure.")
    @GetMapping(path = "/find")
    public List<ConversionResultDto> findSearchData(String measureId, DocumentsToFind find) {
        return conversionResultProcessorService.processOne(measureId, find);

    }

    @Operation(summary = "Find all Reports for batch.",
            description = "Find and return the reports for all the measures in the batch.")
    @GetMapping(path = "/findAll")
    public List<ConversionResultDto> findAll(@RequestParam String batchId) {
        return conversionResultProcessorService.processAllForBatch(batchId);
    }

    public enum DocumentsToFind {
        ALL, LAST
    }
}
