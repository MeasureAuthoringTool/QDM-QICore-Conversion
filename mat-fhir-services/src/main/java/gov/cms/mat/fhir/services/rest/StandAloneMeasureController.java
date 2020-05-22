package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.components.mongo.ThreadSessionKey;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.HapiResourceValidationException;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.service.MeasureDataService;
import gov.cms.mat.fhir.services.service.orchestration.MeasureOrchestrationConversionService;
import gov.cms.mat.fhir.services.service.orchestration.MeasureOrchestrationValidationService;
import gov.cms.mat.fhir.services.service.orchestration.OrchestrationService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;
import java.time.Instant;

@RestController
@RequestMapping(path = "/measure")
@Tag(name = "Measure-Controller", description = "API for Measures")
@Slf4j
public class StandAloneMeasureController implements FhirValidatorProcessor {
    private final MeasureOrchestrationValidationService measureOrchestrationValidationService;
    private final MeasureOrchestrationConversionService measureOrchestrationConversionService;
    private final ConversionResultsService conversionResultsService;
    private final OrchestrationService orchestrationService;

    private final MeasureDataService measureDataService;

    public StandAloneMeasureController(MeasureOrchestrationValidationService measureOrchestrationValidationService,
                                       MeasureOrchestrationConversionService measureOrchestrationConversionService,
                                       ConversionResultsService conversionResultsService, OrchestrationService orchestrationService,
                                       MeasureDataService measureDataService) {
        this.measureOrchestrationValidationService = measureOrchestrationValidationService;
        this.measureOrchestrationConversionService = measureOrchestrationConversionService;
        this.conversionResultsService = conversionResultsService;
        this.orchestrationService = orchestrationService;
        this.measureDataService = measureDataService;
    }


    @Operation(summary = "Orchestrate Stand alone Hapi FHIR Measure with the id",
            description = "Find the HAPI stand alone Measure in Mat convert and persist to FHIR",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Value set found and json returned"),
                    @ApiResponse(responseCode = "404", description = "CqlLibrary is not found in the mat db using the id")})
    @PostMapping("/pushMeasure")
    public String convertStandAloneFromMatToFhir(
            @RequestParam @Min(10) String id,
            @RequestParam(required = false, defaultValue = "MEASURE-STANDALONE-ORCHESTRATION") String batchId) {

        Measure measure = measureDataService.findOneValid(id);

        ThreadSessionKey threadSessionKey = buildThreadSessionKey(id, batchId);

        OrchestrationProperties orchestrationProperties = buildProperties(threadSessionKey, measure);

        boolean valid = orchestrationService.process(orchestrationProperties);

        if (!valid) {
            throw new HapiResourceValidationException(id, "Measure");
        } else {
            measureOrchestrationConversionService.convert(orchestrationProperties);
        }

        return ConversionReporter.getConversionResult().getMeasureConversionResults().getFhirMeasureJson();
    }

    public OrchestrationProperties buildProperties(ThreadSessionKey threadSessionKey, Measure measure) {
        return OrchestrationProperties.builder()
                .showWarnings(Boolean.FALSE)
                .conversionType(ConversionType.CONVERSION)
                .includeStdLibs(false)
                .matMeasure(measure)
                .xmlSource(XmlSource.MEASURE)
                .threadSessionKey(threadSessionKey)
                .build();
    }

    public ThreadSessionKey buildThreadSessionKey(String id,
                                                  String batchId) {
        return ConversionReporter.setInThreadLocal(id,
                batchId,
                conversionResultsService,
                Instant.now(),
                ConversionType.CONVERSION,
                XmlSource.MEASURE,
                Boolean.FALSE,
                "");
    }
}
