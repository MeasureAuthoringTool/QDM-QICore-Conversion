package gov.cms.mat.fhir.services.rest;


import gov.cms.mat.cql.CqlParser;
import gov.cms.mat.cql.elements.IncludeProperties;
import gov.cms.mat.cql.elements.LibraryProperties;
import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryReferences;
import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryResult;
import gov.cms.mat.fhir.services.components.library.FhirCqlLibraryFileHandler;
import gov.cms.mat.fhir.services.exceptions.CqlLibraryNotFoundException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.rest.support.CqlVersionConverter;
import gov.cms.mat.fhir.services.service.CqlLibraryDataService;
import gov.cms.mat.fhir.services.summary.CqlLibraryFindData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static gov.cms.mat.fhir.services.translate.MatLibraryTranslator.CQL_CONTENT_TYPE;

@RestController
@RequestMapping(path = "/library/find")
@Tag(name = "LibraryFinder-Controller", description = "API for finding MAT Libraries ")
@Slf4j
public class LibraryFinderController implements CqlVersionConverter {
    private final CqlLibraryDataService cqlLibraryDataService;
    private final HapiFhirServer hapiFhirServer;
    private final FhirCqlLibraryFileHandler fhirCqlLibraryFileHandler;

    public LibraryFinderController(CqlLibraryDataService cqlLibraryDataService,
                                   HapiFhirServer hapiFhirServer,
                                   FhirCqlLibraryFileHandler fhirCqlLibraryFileHandler) {
        this.cqlLibraryDataService = cqlLibraryDataService;
        this.hapiFhirServer = hapiFhirServer;
        this.fhirCqlLibraryFileHandler = fhirCqlLibraryFileHandler;
    }

    @Operation(summary = "Find Cql-XML in mat.",
            description = "Find Cql-XML in mat using the request params")
    @GetMapping("/mat")
    public String findLibraryXml(@RequestParam String qdmVersion, @RequestParam String name, @RequestParam String version) {
        CqlLibraryFindData data = CqlLibraryFindData.builder()
                .qdmVersion(qdmVersion)
                .name(name)
                .matVersion(convertVersionToBigDecimal(version))
                .version(version)
                .build();

        CqlLibrary cqlLibrary = cqlLibraryDataService.findCqlLibrary(data);

        if (StringUtils.isEmpty(cqlLibrary.getCqlXml())) {
            throw new CqlLibraryNotFoundException("Xml is missing for library id: ", cqlLibrary.getId());
        } else {
            return cqlLibrary.getCqlXml();
        }
    }

    @Operation(summary = "Find Cql-XML in mat.",
            description = "Find Cql-XML in mat using the request params")
    @GetMapping("/hapi")
    public String findLibraryHapiCql(@RequestParam String name, @RequestParam String version) {

        Bundle bundle = hapiFhirServer.fetchLibraryBundleByVersionAndName(version, name);

        if (CollectionUtils.isEmpty(bundle.getEntry())) {
            throw new CqlLibraryNotFoundException("Cannot find library for name: " + name + ", version: " + version);
        } else {
            Optional<Library> optional = hapiFhirServer.findResourceInBundle(bundle, Library.class);

            if (optional.isPresent()) {
                Library library = optional.get();
                List<Attachment> attachments = library.getContent();

                if (CollectionUtils.isEmpty(attachments)) {
                    throw new CqlLibraryNotFoundException("Cannot find attachments for library name: " + name + ", version: " + version);
                }

                Attachment cql = attachments.stream()
                        .filter(a -> a.getContentType().equals(CQL_CONTENT_TYPE))
                        .findFirst()
                        .orElseThrow(() -> new CqlLibraryNotFoundException("Cannot find attachment type " + CQL_CONTENT_TYPE +
                                " for library  name: " + name + ", version: " + version));

                return new String(cql.getData());


            } else {
                throw new CqlLibraryNotFoundException("Cannot find library in bundle for name: " + name + ", version: " + version);
            }
        }
    }

    @Operation(summary = "Find Include Library in FHIR.",
            description = "Finding included FHIR libraries using the main measure library")
    @PostMapping(path = "/includeLibrarySearch", consumes = "text/plain", produces = "application/json")
    public FhirIncludeLibraryResult findIncludedFhirLibraries(@RequestBody String cqlContent) { // for fhr cql only
        FhirIncludeLibraryResult res = new FhirIncludeLibraryResult();
        List<FhirIncludeLibraryReferences> includeRefs = new ArrayList<FhirIncludeLibraryReferences>();
        String libraryName = "";
        String libraryVersion = "";
        boolean result = true;
        try {
            CqlParser cqlParser = new CqlParser(cqlContent);
            LibraryProperties props = cqlParser.getLibrary();
            libraryName = props.getName();
            libraryVersion = props.getVersion();

            res.setLibraryName(libraryName);
            res.setLibraryVersion(libraryVersion);

            List<IncludeProperties> includes =   cqlParser.getIncludes();
            Iterator iter = includes.iterator();


            while (iter.hasNext()) {
                IncludeProperties include = (IncludeProperties)iter.next();
                FhirIncludeLibraryReferences iRefs = new FhirIncludeLibraryReferences();
                String includeLibraryName = include.getName();
                String includeLibraryVersion = include.getVersion();
                iRefs.setName(includeLibraryName);
                iRefs.setVersion(includeLibraryVersion);                
                boolean includeResults = true;
                String referenceEndpoint = "";
                try {
                    Bundle bundle = hapiFhirServer.fetchLibraryBundleByVersionAndName(includeLibraryVersion, includeLibraryName);
                    referenceEndpoint = bundle.getEntry().get(0).getFullUrl();
                    iRefs.setReferenceEndpoint(referenceEndpoint);
                }
                catch(Exception nx) {
                    nx.printStackTrace();
                    includeResults = false;
                    result = includeResults;
                }
                iRefs.setSearchResult(includeResults);
                includeRefs.add(iRefs);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            log.warn("Failed to process library "+libraryName +" "+libraryVersion);
        }

        res.setOutcome(result);
        res.setLibraryReferences(includeRefs);
        return res;
    }
 

    @Operation(summary = "load.",
            description = "Load")
    @GetMapping("/load")
    public String load() {
        fhirCqlLibraryFileHandler.loaLibs();
        return "OK";
    }
}
