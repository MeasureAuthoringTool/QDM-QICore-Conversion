package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.rest.dto.CqlConversionReportError;
import gov.cms.mat.fhir.services.service.LibraryErrorReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/libraryErrorReport")
@Tag(name = "LibraryErrorReport-Controller", description = "API for for analyzing conversion results.")
@Slf4j
public class LibraryErrorReportController {
    private final LibraryErrorReportService libraryErrorReportService;

    public LibraryErrorReportController(LibraryErrorReportService libraryErrorReportService) {
        this.libraryErrorReportService = libraryErrorReportService;
    }

    @Operation(summary = "Find all Reports for batch.",
            description = "Find and return the reports for all the measures in the batch.")
    @GetMapping(path = "/libraryErrors")
    public List<CqlConversionReportError> findAllLibraryErrors(@RequestParam String batchId) {
        return libraryErrorReportService.processCountForBatch(batchId);
    }
}
