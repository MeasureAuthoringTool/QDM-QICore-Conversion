package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.packaging.LibraryPackagerService;
import gov.cms.mat.fhir.services.service.packaging.dto.PackageFormat;
import gov.cms.mat.fhir.services.service.packaging.dto.LibraryPackageFullData;
import gov.cms.mat.fhir.services.service.packaging.dto.LibraryPackageFullHapi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static gov.cms.mat.fhir.services.service.packaging.dto.PackageFormat.JSON;
import static gov.cms.mat.fhir.services.service.packaging.dto.PackageFormat.XML;

@RestController
@RequestMapping(path = "/library/packager")
@Tag(name = "Library-Controller", description = "API for packaging libraries")
@Slf4j
public class LibraryPackagerController {
    private final LibraryPackagerService libraryPackagerService;
    private final HapiFhirServer hapiFhirServer;

    public LibraryPackagerController(LibraryPackagerService libraryPackagerService,
                                     HapiFhirServer hapiFhirServer) {
        this.libraryPackagerService = libraryPackagerService;
        this.hapiFhirServer = hapiFhirServer;
    }

    @Operation(summary = "Minimum Package of FHIR  Library",
            description = "Minimum Packaging of  Library in json")
    @GetMapping(value = "/minimum/json", produces = {"application/json"})
    @ResponseBody
    public String packageMinimumJson(@RequestParam String id) {
        try {
            return packageLibraryMinimum(id, JSON);
        } catch (RuntimeException r) {
            log.error("packageMinimumJson", r);
            throw r;
        }
    }

    @Operation(summary = "Full json Package of FHIR  Library",
            description = "Minimum Packaging of  Library in json")
    @GetMapping(value = "/full/json")
    @ResponseBody
    public LibraryPackageFullData packageFullJson(@RequestParam String id) {
        try {
            return packageLibraryFull(id, JSON);
        } catch (RuntimeException r) {
            log.error("packageFullJson", r);
            throw r;
        }
    }

    @Operation(summary = "Full xml Package of FHIR  Library",
            description = "Minimum Packaging of  Library in xml")
    @GetMapping(value = "/full/xml")
    @ResponseBody
    public LibraryPackageFullData packageFullXml(@RequestParam String id) {
        try {
            return packageLibraryFull(id, XML);
        } catch (RuntimeException r) {
            log.error("packageFullXml", r);
            throw r;
        }
    }

    @Operation(summary = "Minimum Package of FHIR  Library",
            description = "Minimum Packaging of  Library in json")
    @GetMapping(value = "/minimum/xml", produces = {"application/json"})
    @ResponseBody
    public String packageMinimumXML(@RequestParam String id) {
        try {
            return packageLibraryMinimum(id, XML);
        } catch (RuntimeException r) {
            log.error("packageMinimumXML", r);
            throw r;
        }
    }

    private String packageLibraryMinimum(String id, PackageFormat format) {
        return hapiFhirServer.formatResource(libraryPackagerService.packageMinimum(id), format);
    }

    private LibraryPackageFullData packageLibraryFull(String id, PackageFormat format) {
        LibraryPackageFullHapi libraryPackageFullHapi = libraryPackagerService.packageFull(id);

        String libraryData = hapiFhirServer.formatResource(libraryPackageFullHapi.getLibrary(), format);
        String bundleData = hapiFhirServer.formatResource(libraryPackageFullHapi.getIncludeBundle(), format);

        return LibraryPackageFullData.builder()
                .library(libraryData)
                .includeBundle(bundleData)
                .build();

    }
}
