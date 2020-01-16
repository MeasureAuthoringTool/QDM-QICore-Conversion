package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.rest.dto.ConversionResultDto;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultProcessorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public Set<String> findMissingValueSets() {
        return conversionResultProcessorService.findMissingValueSets();
    }

    @Operation(summary = "Find report for a measure.",
            description = "Find and return a error report for a measure.")
    @GetMapping(path = "/find")
    public ConversionResultDto findSearchData(String measureId) {
        // return conversionResultProcessorService.process(measureId);
        throw new UnsupportedOperationException("TODO"); // needs to be list or get last
    }

    @Operation(summary = "Find all Reports.",
            description = "Find and return the error reports for all the measures.")
    @GetMapping(path = "/findAll")
    public List<ConversionResultDto> findAll() {
        return conversionResultProcessorService.processAll();
    }
}
