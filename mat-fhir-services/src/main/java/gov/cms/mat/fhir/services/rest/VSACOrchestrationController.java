package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.rest.dto.ConversionResultDto;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.mongo.*;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.MeasureNotFoundException;
import gov.cms.mat.fhir.services.exceptions.MeasureReleaseVersionInvalidException;
import gov.cms.mat.fhir.services.service.MeasureDataService;
import gov.cms.mat.fhir.services.service.orchestration.OrchestrationService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;
import java.time.Instant;

import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.*;

@RestController
@RequestMapping(path = "/orchestration/vsac")
@Tag(name = "Orchestration-Controller",
        description = "API for converting VSAC ValueSets to FHIR executing all validations and services to perform this task")
@Slf4j
public class VSACOrchestrationController {
    private final OrchestrationService orchestrationService;
    private final ConversionResultProcessorService conversionResultProcessorService;
    private final MeasureDataService measureDataService;
    private final ConversionResultsService conversionResultsService;

    public VSACOrchestrationController(OrchestrationService orchestrationService,
                                   ConversionResultProcessorService conversionResultProcessorService,
                                   MeasureDataService measureDataService,
                                   ConversionResultsService conversionResultsService) {
        this.orchestrationService = orchestrationService;
        this.conversionResultProcessorService = conversionResultProcessorService;
        this.measureDataService = measureDataService;
        this.conversionResultsService = conversionResultsService;
    }

    @Operation(summary = "Orchestrate Measure in MAT to FHIR.",
            description = "Orchestrate one Measure in the MAT Database and verify and persist (if applicable) to the HAPI FHIR Database. " +
                    "Method returns the fhir conversion results.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Orchestration ran and report generated"),
                    @ApiResponse(responseCode = "412", description = "Measure is not valid for fhir conversion "),
                    @ApiResponse(responseCode = "404", description = "Measure is not found in the mat db using the id")})
    @PutMapping
    public ConversionResultDto translateMeasureById(
            @RequestParam @Min(10) String id,
            @RequestParam ConversionType conversionType,
            @RequestParam(required = false, defaultValue = "SIMPLE") XmlSource xmlSource,
            @RequestParam(required = false, defaultValue = "ORCHESTRATION") String batchId,
            @RequestParam(required = false, defaultValue = "false") boolean showWarnings,
            @RequestParam String vsacGrantingTicket) {
        ThreadSessionKey threadSessionKey =
                ConversionReporter.setInThreadLocal(id,
                        batchId,
                        conversionResultsService,
                        Instant.now(),
                        conversionType,
                        xmlSource,
                        showWarnings);
        try {
            Measure matMeasure;

            try {
                matMeasure = measureDataService.findOneValid(id);
            } catch (MeasureReleaseVersionInvalidException e) {
                ConversionReporter.setTerminalMessage(e.getMessage(), MEASURE_RELEASE_VERSION_INVALID);
                return conversionResultProcessorService.process(threadSessionKey);
            } catch (MeasureNotFoundException e) {
                ConversionReporter.setTerminalMessage(e.getMessage(), MEASURE_NOT_FOUND);
                return conversionResultProcessorService.process(threadSessionKey);
            }

            OrchestrationProperties orchestrationProperties = OrchestrationProperties.builder()
                    .matMeasure(matMeasure)
                    .conversionType(conversionType)
                    .xmlSource(xmlSource)
                    .threadSessionKey(threadSessionKey)
                    .build();

            return process(orchestrationProperties, vsacGrantingTicket);
        } finally {
            ConversionReporter.removeInThreadLocalAndComplete();
        }
    }

    public ConversionResultDto process(OrchestrationProperties orchestrationProperties, String vsacGrantingTicket) {
        log.info("Started Orchestrating Measure key: {}", orchestrationProperties.getThreadSessionKey());

        orchestrationService.process(orchestrationProperties, vsacGrantingTicket);

        ConversionResult conversionResult = ConversionReporter.getConversionResult();

        if (conversionResult.getOutcome() == null) {
            if (haveConversionResultError(conversionResult)) {
                ConversionReporter.setTerminalMessage(SUCCESS_WITH_ERROR.name(), SUCCESS_WITH_ERROR);
            } else {
                ConversionReporter.setTerminalMessage(SUCCESS.name(), SUCCESS);
            }
        }

        return conversionResultProcessorService.process(orchestrationProperties.getThreadSessionKey());
    }


    private boolean haveConversionResultError(ConversionResult conversionResult) {
        var optionalValueSetError = conversionResult.getValueSetConversionResults().stream()
                .filter(l -> BooleanUtils.isNotTrue(l.getSuccess()))
                .findFirst();

        if (optionalValueSetError.isPresent()) {
            return true;
        }

        var optionalLibraryError = conversionResult.getLibraryConversionResults().stream()
                .filter(l -> BooleanUtils.isNotTrue(l.getSuccess()))
                .findFirst();

        if (optionalLibraryError.isPresent()) {
            return true;
        }

        return conversionResult.getMeasureConversionResults() == null ||
                BooleanUtils.isNotTrue(conversionResult.getMeasureConversionResults().getSuccess());
    }
}
