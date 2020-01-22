package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.services.exceptions.HapiResourceNotFoundException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.translate.LibraryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Library;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/library")
@Tag(name = "Library-Controller", description = "API for converting MAT Libraries to FHIR")
@Slf4j
public class LibraryController implements FhirValidatorProcessor {
    private final HapiFhirServer hapiFhirServer;
    private final LibraryMapper libraryMapper;

    public LibraryController(HapiFhirServer hapiFhirServer,
                             LibraryMapper libraryMapper) {

        this.hapiFhirServer = hapiFhirServer;
        this.libraryMapper = libraryMapper;
    }

    @Operation(summary = "Find a Hapi FHIR Library with the id",
            description = "Find the Hapi Library converted to json",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Value set found and json returned"),
                    @ApiResponse(responseCode = "404", description = "Measure is not found in the mat db using the id")})
    @GetMapping(path = "/findOne")
    public String findOne(String id) {
        Library library = hapiFhirServer.fetchHapiLibrary(id)
                .orElseThrow(() -> new HapiResourceNotFoundException(id, "Library"));

        return hapiFhirServer.toJson(library);
    }

    @Operation(summary = "Count of persisted FHIR Libraries.",
            description = "The count of all the Libraries in the HAPI FHIR Database.")
    @GetMapping(path = "/count")
    public int countValueSets() {
        return libraryMapper.count();
    }

    @Operation(summary = "Delete all persisted FHIR Libraries.",
            description = "Delete all the Libraries in the HAPI FHIR Database. (chiefly used for testing). " +
                    "Returns the count of resources deleted.")
    @DeleteMapping(path = "/deleteAll")
    public int deleteValueSets() {
        return libraryMapper.deleteAll();
    }
}
