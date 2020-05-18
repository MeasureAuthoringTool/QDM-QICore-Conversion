package gov.cms.mat.fhir.services.rest;


import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryResult;
import gov.cms.mat.fhir.rest.dto.cql.CqlPayload;
import gov.cms.mat.fhir.services.components.fhir.FhirIncludeLibraryProcessor;
import gov.cms.mat.fhir.services.rest.support.CqlVersionConverter;
import gov.cms.mat.fhir.services.service.LibraryFinderService;
import gov.cms.mat.fhir.services.summary.CqlLibraryFindData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping(path = "/library/find")
@Tag(name = "Library-Controller", description = "API for Libraries ")
@Slf4j
public class LibraryFinderController implements CqlVersionConverter {
    private final LibraryFinderService libraryFinderService;
    private final FhirIncludeLibraryProcessor fhirIncludeLibraryProcessor;

    public LibraryFinderController(LibraryFinderService libraryFinderService,
                                   FhirIncludeLibraryProcessor fhirIncludeLibraryProcessor) {
        this.libraryFinderService = libraryFinderService;
        this.fhirIncludeLibraryProcessor = fhirIncludeLibraryProcessor;
    }

    @Operation(summary = "Find Cql-XML in mat.",
            description = "Find Cql-XML in mat using the request params")
    @GetMapping("/mat")
    public CqlPayload findLibraryXml(@RequestParam String qdmVersion,
                                     @RequestParam String name,
                                     @RequestParam String version,
                                     @RequestParam String type) {

        Pair<BigDecimal, Integer> pair = versionToVersionAndRevision(version);

        CqlLibraryFindData data = CqlLibraryFindData.builder()
                .qdmVersion(qdmVersion)
                .name(name)
                .matVersion(convertVersionToBigDecimal(version))
                .version(version)
                .type(type)
                .pair(pair)
                .build();

        return libraryFinderService.findLibrary(data);
    }

    @Operation(summary = "Find Cql-XML in HAPI_FHIR.",
            description = "Find Cql-XML in hapi using the request params")
    @GetMapping("/hapi")
    public String findLibraryHapiCql(@RequestParam String name, @RequestParam String version) {
        return libraryFinderService.getCqlFromFire(name, version);
    }

    @Operation(summary = "Find Include Library in FHIR.",
            description = "Finding included FHIR libraries using the main measure library")
    @PostMapping(path = "/includeLibrarySearch", consumes = "text/plain", produces = "application/json")
    public FhirIncludeLibraryResult findIncludedFhirLibraries(@RequestBody String cqlContent) { // for fhr cql only
        return fhirIncludeLibraryProcessor.findIncludedFhirLibraries(cqlContent);
    }

}
