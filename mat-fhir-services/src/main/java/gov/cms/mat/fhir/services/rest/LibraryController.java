package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.services.exceptions.HapiResourceNotFoundException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
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

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/library")
@Tag(name = "Library-Controller", description = "API for converting MAT Libraries to FHIR")
@Slf4j
public class LibraryController {
    private final HapiFhirServer hapiFhirServer;
    private final LibraryMapper libraryMapper;

    public LibraryController(@Valid HapiFhirServer hapiFhirServer,
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

    @Operation(summary = "Find a Hapi FHIR Library's cql with the name and version",
            description = "Find the Hapi Library and show cql",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Library found and cql returned"),
                    @ApiResponse(responseCode = "404", description = "Library is not found or does contain cql")})
    @GetMapping(path = "/findCql")
    public String findCql(String name, String version) {
        var optionalLibrary = hapiFhirServer.fetchHapiLibrary(name, version);

        if (optionalLibrary.isEmpty()) {
            throw new HapiResourceNotFoundException(name, version, "Library");
        } else {
            Library library = optionalLibrary.get();

            var optionalCql = library.getContent().stream()
                    .filter(c -> c.getContentType().equals("text/cql"))
                    .map(c -> new String(c.getData()))
                    .findFirst();

            return optionalCql.orElseThrow(() -> new HapiResourceNotFoundException("Library does not contain cql"));
        }
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
