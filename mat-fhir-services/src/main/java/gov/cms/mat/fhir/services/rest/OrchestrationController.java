package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.rest.dto.ConversionResultDto;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultProcessorService;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.service.MeasureDataService;
import gov.cms.mat.fhir.services.service.orchestration.OrchestrationService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/orchestration/measure")
@Tag(name = "Orchestration-Controller",
        description = "API for converting MAT Measures to FHIR executing all validations and services to perform this task")
@Slf4j
public class OrchestrationController {
    private final OrchestrationService orchestrationService;
    private final ConversionResultProcessorService conversionResultProcessorService;
    private final MeasureDataService measureDataService;


    public OrchestrationController(OrchestrationService orchestrationService,
                                   ConversionResultProcessorService conversionResultProcessorService,
                                   MeasureDataService measureDataService) {
        this.orchestrationService = orchestrationService;
        this.conversionResultProcessorService = conversionResultProcessorService;
        this.measureDataService = measureDataService;
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
            @RequestParam String id,
            @RequestParam ConversionType conversionType,
            @RequestParam(required = false, defaultValue = "SIMPLE") XmlSource xmlSource) {

        Measure matMeasure = measureDataService.findOneValid(id);
        log.info("Orchestrating Measure: {}", id);

        OrchestrationProperties orchestrationProperties = OrchestrationProperties.builder()
                .matMeasure(matMeasure)
                .conversionType(conversionType)
                .xmlSource(xmlSource)
                .build();
        orchestrationService.process(orchestrationProperties);

        return conversionResultProcessorService.process(id);
    }
}
