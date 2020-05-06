package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.packaging.PackageFormat;
import gov.cms.mat.fhir.services.service.packaging.StandAloneLibraryPackagerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static gov.cms.mat.fhir.services.service.packaging.PackageFormat.JSON;
import static gov.cms.mat.fhir.services.service.packaging.PackageFormat.XML;

@RestController
@RequestMapping(path = "/library/packager")
@Tag(name = "StandAloneLibraryPackager-Controller", description = "API for packaging stand alone Libraries to FHIR")
@Slf4j
public class StandAloneLibraryPackagerController {
    private final StandAloneLibraryPackagerService standAloneLibraryPackagerService;
    private final HapiFhirServer hapiFhirServer;

    public StandAloneLibraryPackagerController(StandAloneLibraryPackagerService standAloneLibraryPackagerService,
                                               HapiFhirServer hapiFhirServer) {
        this.standAloneLibraryPackagerService = standAloneLibraryPackagerService;
        this.hapiFhirServer = hapiFhirServer;
    }

    @Operation(summary = "Minimum Package of FHIR Stand Alone Library",
            description = "Minimum Packaging of Stand Alone Library in json")
    @GetMapping(value = "/minimum/json", produces = {"application/json"})
    @ResponseBody
    public String packageMinimumJson(@RequestParam String id) {
        return packageLibraryMinimum(id, JSON);
    }


    @Operation(summary = "Full json Package of FHIR Stand Alone Library",
            description = "Minimum Packaging of Stand Alone Library in json")
    @GetMapping(value = "/full/json", produces = {"application/json"})
    @ResponseBody
    public String packageMinimumFullJson(@RequestParam String id) {
        return packageLibraryFull(id, JSON);
    }


    @Operation(summary = "Full xml Package of FHIR Stand Alone Library",
            description = "Minimum Packaging of Stand Alone Library in xml")
    @GetMapping(value = "/full/xml", produces = {"application/json"})
    @ResponseBody
    public String packageMinimumFullXml(@RequestParam String id) {
        return packageLibraryFull(id, XML);
    }

    @Operation(summary = "Minimum Package of FHIR Stand Alone Library",
            description = "Minimum Packaging of Stand Alone Library in json")
    @GetMapping(value = "/minimum/xml", produces = {"application/xml"})
    @ResponseBody
    public String packageMinimumXML(@RequestParam String id) {
        return packageLibraryMinimum(id, XML);
    }

    private String packageLibraryMinimum(String id, PackageFormat format) {
        return hapiFhirServer.formatResource(standAloneLibraryPackagerService.packageMinimum(id), format);
    }

    private String packageLibraryFull(String id, PackageFormat format) {
        return hapiFhirServer.formatResource(standAloneLibraryPackagerService.packageFull(id), format);
    }
}
