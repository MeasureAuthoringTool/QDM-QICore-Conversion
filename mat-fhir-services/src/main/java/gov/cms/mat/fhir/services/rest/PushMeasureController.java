package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.rest.dto.PushValidationResult;
import gov.cms.mat.fhir.services.components.reporting.ConversionReporter;
import gov.cms.mat.fhir.services.components.reporting.ConversionResultsService;
import gov.cms.mat.fhir.services.components.reporting.ThreadSessionKey;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.service.orchestration.PushMeasureService;
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
public class PushMeasureController {
    private final ConversionResultsService conversionResultsService;
    private final PushMeasureService pushMeasureService;

    public PushMeasureController(ConversionResultsService conversionResultsService,
                                 PushMeasureService pushMeasureService) {

        this.conversionResultsService = conversionResultsService;
        this.pushMeasureService = pushMeasureService;
    }

    @Operation(summary = "Orchestrate Stand alone Hapi FHIR Measure with the id",
            description = "Find the HAPI stand alone Measure in Mat convert and persist to FHIR",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Measure is found in mat and updated in hapi and json returned"),
                    @ApiResponse(responseCode = "404", description = "Measure is not found in the mat db using the id")})
    @PostMapping("/pushMeasure")
    public PushValidationResult pushMeasure (
            @RequestParam @Min(10) String id,
            @RequestParam(required = false, defaultValue = "PUSH-MEASURE-ORCHESTRATION") String batchId) {
        try {

            ThreadSessionKey threadSessionKey = buildThreadSessionKey(id, batchId);
            OrchestrationProperties orchestrationProperties = buildProperties(threadSessionKey);

            return pushMeasureService.convert(id, orchestrationProperties);
        } catch (RuntimeException e) {
            log.error("pushMeasure",e);
            throw e;
        }
    }

    private OrchestrationProperties buildProperties(ThreadSessionKey threadSessionKey) {
        return OrchestrationProperties.builder()
                .showWarnings(Boolean.FALSE)
                .conversionType(ConversionType.CONVERSION)
                .includeStdLibs(false)
                .xmlSource(XmlSource.MEASURE)
                .isPush(true)
                .threadSessionKey(threadSessionKey)
                .build();
    }

    private ThreadSessionKey buildThreadSessionKey(String id,
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
