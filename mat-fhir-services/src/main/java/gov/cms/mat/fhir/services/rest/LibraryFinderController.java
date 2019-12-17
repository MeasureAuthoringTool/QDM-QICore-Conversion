package gov.cms.mat.fhir.services.rest;


import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.services.exceptions.CqlLibraryNotFoundException;
import gov.cms.mat.fhir.services.rest.support.CqlVersionConverter;
import gov.cms.mat.fhir.services.service.CqlLibraryService;
import gov.cms.mat.fhir.services.summary.CqlLibraryFindData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/library/find")
@Tag(name = "LibraryFinder-Controller", description = "API for finding MAT Libraries ")
@Slf4j
public class LibraryFinderController implements CqlVersionConverter {

    private final CqlLibraryService cqlLibraryService;

    public LibraryFinderController(CqlLibraryService cqlLibraryService) {
        this.cqlLibraryService = cqlLibraryService;
    }

    @Operation(summary = "Find Cql-XML in mat.",
            description = "Find Cql-XML in mat using the request params")
    @PutMapping
    public String findLibraryXml(@RequestParam String qdmVersion, @RequestParam String name, @RequestParam String version) {

        CqlLibraryFindData data = CqlLibraryFindData.builder()
                .qdmVersion(qdmVersion)
                .name(name)
                .version(convertVersionToBigDecimal(version))
                .build();

        CqlLibrary cqlLibrary = cqlLibraryService.findCqlLibrary(data);

        if (StringUtils.isEmpty(cqlLibrary.getCqlXml())) {
            throw new CqlLibraryNotFoundException("Xml is missing for library id: ", cqlLibrary.getId());
        } else {
            return cqlLibrary.getCqlXml();
        }

    }

}
