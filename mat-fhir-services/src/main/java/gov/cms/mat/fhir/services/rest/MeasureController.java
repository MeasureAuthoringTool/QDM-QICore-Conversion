package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.services.exceptions.HapiResourceNotFoundException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.translate.MeasureMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Measure;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/measure")
@Tag(name = "Measure-Controller", description = "API for converting MAT Measures to FHIR")
@Slf4j
public class MeasureController {
    private final FhirValidatorProcessor fhirValidationProcessor;
    private final HapiFhirServer hapiFhirServer;
    private final MeasureMapper measureMapper;

    public MeasureController(FhirValidatorProcessor fhirValidationProcessor, HapiFhirServer hapiFhirServer,
                             MeasureMapper measureMapper) {
        this.fhirValidationProcessor = fhirValidationProcessor;
        this.hapiFhirServer = hapiFhirServer;
        this.measureMapper = measureMapper;
    }

    @Operation(summary = "Find a Hapi FHIR Library with the id",
            description = "Find the Hapi Library converted to json",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Value set found and json returned"),
                    @ApiResponse(responseCode = "404", description = "Measure is not found in the mat db using the id")})
    @GetMapping(path = "/findOne")
    public String findOne(String id) {
        try {
            Measure measure = hapiFhirServer.fetchHapiMeasure(id)
                    .orElseThrow(() -> new HapiResourceNotFoundException(id, "Measure"));
            return hapiFhirServer.toJson(measure);
        } catch (RuntimeException r) {
            log.error("findOne", r);
            throw r;
        }
    }

    @Operation(summary = "Count of persisted FHIR Measures.",
            description = "The count of all the Measures in the HAPI FHIR Database.")
    @GetMapping(path = "/count")
    public int countMeasures() {
        try {
            return measureMapper.count();
        } catch (RuntimeException r) {
            log.error("countMeasures", r);
            throw r;
        }

    }

    @Operation(summary = "Delete all persisted FHIR Measures.",
            description = "Delete all the Measures in the HAPI FHIR Database.")
    @DeleteMapping(path = "/deleteAll")
    public int deleteMeasures() {
        try {
            return measureMapper.deleteAll();
        } catch (RuntimeException r) {
            log.error("deleteMeasures", r);
            throw r;
        }
    }
}
