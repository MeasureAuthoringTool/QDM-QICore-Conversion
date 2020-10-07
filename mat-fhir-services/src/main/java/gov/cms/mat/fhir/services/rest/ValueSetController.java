package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.services.exceptions.HapiResourceNotFoundException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.service.ValueSetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/valueSet")
@Tag(name = "ValueSet-Controller", description = "API for converting MAT ValueSets to FHIR.")
@Slf4j
public class ValueSetController implements FhirValidatorProcessor {
    private final ValueSetService valueSetService;

    private final HapiFhirServer hapiFhirServer;

    public ValueSetController(ValueSetService valueSetService,
                              HapiFhirServer hapiFhirServer) {
        this.valueSetService = valueSetService;
        this.hapiFhirServer = hapiFhirServer;
    }

    @Operation(summary = "Find a Hapi FHIR ValueSet with the oid (id)",
            description = "Find the Hapi ValueSet converted to json",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Value set found and json returned"),
                    @ApiResponse(responseCode = "404", description = "Measure is not found in the mat db using the id")})
    @GetMapping(path = "/findOne")
    public String findOne(String oid) {
        ValueSet valueSet = hapiFhirServer.fetchHapiValueSet(oid)
                .orElseThrow(() -> new HapiResourceNotFoundException(oid, "ValueSet"));

        return hapiFhirServer.toJson(valueSet);
    }

    @Operation(summary = "Count of persisted FHIR ValueSets.",
            description = "The count of all the ValueSets in the HAPI FHIR Database.")
    @GetMapping(path = "/count")
    public int countValueSets() {
        return valueSetService.count();
    }

    @Operation(summary = "Delete all persisted FHIR ValueSets.",
            description = "Delete all the ValueSets in the HAPI FHIR Database.")
    @DeleteMapping(path = "/deleteAll")
    public int deleteValueSets() {
        return valueSetService.deleteAll();
    }
}