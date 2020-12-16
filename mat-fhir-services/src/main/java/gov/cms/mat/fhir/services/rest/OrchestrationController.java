package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.rest.dto.ConversionOutcome;
import gov.cms.mat.fhir.rest.dto.ConversionResultDto;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.reporting.ConversionReporter;
import gov.cms.mat.fhir.services.components.reporting.ConversionResultProcessorService;
import gov.cms.mat.fhir.services.components.reporting.ConversionResultsService;
import gov.cms.mat.fhir.services.components.reporting.ThreadSessionKey;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.MeasureNotFoundException;
import gov.cms.mat.fhir.services.exceptions.MeasureReleaseVersionInvalidException;
import gov.cms.mat.fhir.services.rest.support.OrchestrationParameterChecker;
import gov.cms.mat.fhir.services.service.MeasureDataService;
import gov.cms.mat.fhir.services.service.orchestration.OrchestrationService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;
import java.time.Instant;

import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.MEASURE_NOT_FOUND;
import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.MEASURE_RELEASE_VERSION_INVALID;

@RestController
@RequestMapping(path = "/orchestration/measure")
@Tag(name = "Orchestration-Controller",
        description = "API for converting MAT Measures to FHIR executing all validations and services to perform this task")
@Slf4j
public class OrchestrationController implements OrchestrationParameterChecker {
    private final OrchestrationService orchestrationService;
    private final ConversionResultProcessorService conversionResultProcessorService;
    private final MeasureDataService measureDataService;
    private final ConversionResultsService conversionResultsService;

    public OrchestrationController(OrchestrationService orchestrationService,
                                   ConversionResultProcessorService conversionResultProcessorService,
                                   MeasureDataService measureDataService,
                                   ConversionResultsService conversionResultsService) {
        this.orchestrationService = orchestrationService;
        this.conversionResultProcessorService = conversionResultProcessorService;
        this.measureDataService = measureDataService;
        this.conversionResultsService = conversionResultsService;
    }

    @Operation(summary = "Orchestrate Measure in MAT to FHIR.",
            description = "Orchestrate one Measure in the MAT Database and verify and persist (if applicable) to the " +
                    "HAPI FHIR Database. Method returns the fhir conversion results.")
    @PutMapping
    public ConversionResultDto translateMeasureById(
            @RequestParam @Min(10) String id,
            @RequestParam ConversionType conversionType,
            @RequestParam(required = false, defaultValue = "SIMPLE") XmlSource xmlSource,
            @RequestParam(required = false, defaultValue = "MEASURE-ORCHESTRATION") String batchId,
            @RequestParam(required = false, defaultValue = "false") boolean showWarnings,
            @RequestParam(required = false, defaultValue = "") String vsacGrantingTicket) {

        checkParameters(xmlSource, conversionType);

        ThreadSessionKey threadSessionKey =
                ConversionReporter.setInThreadLocal(id,
                        batchId,
                        conversionResultsService,
                        Instant.now(),
                        conversionType,
                        xmlSource,
                        showWarnings,
                        vsacGrantingTicket);
        OrchestrationProperties orchestrationProperties = null;

        try {
            Measure matMeasure = find(id);

            if (matMeasure == null) {
                return conversionResultProcessorService.process(threadSessionKey);
            } else {
                orchestrationProperties = OrchestrationProperties.builder()
                        .showWarnings(showWarnings)
                        .includeStdLibs(true)
                        .matMeasure(matMeasure)
                        .conversionType(conversionType)
                        .xmlSource(xmlSource)
                        .threadSessionKey(threadSessionKey)
                        .vsacGrantingTicket(vsacGrantingTicket)
                        .build();

                return process(orchestrationProperties);
            }

        } finally {
            ConversionReporter.removeInThreadLocalAndComplete();
        }
    }

    private Measure find(String id) {
        try {
            return measureDataService.findOneValid(id);
        } catch (MeasureReleaseVersionInvalidException e) {
            ConversionReporter.setTerminalMessage(e.getMessage(), MEASURE_RELEASE_VERSION_INVALID);
            return null;
        } catch (MeasureNotFoundException e) {
            ConversionReporter.setTerminalMessage(e.getMessage(), MEASURE_NOT_FOUND);
            return null;
        }
    }

    public ConversionResultDto process(OrchestrationProperties orchestrationProperties) {
        log.debug("Started Orchestrating Measure key: {}", orchestrationProperties.getThreadSessionKey());
        orchestrationService.process(orchestrationProperties);
        return conversionResultProcessorService.process(orchestrationProperties.getThreadSessionKey());
    }
}


