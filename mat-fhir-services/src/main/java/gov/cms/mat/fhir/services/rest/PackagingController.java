package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.rest.packaging.FhirPackage;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.service.orchestration.PackagingOrchestrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/package")
@Tag(name = "Packaging-Controller", description = "API for packaging measures")
@Slf4j
public class PackagingController implements FhirValidatorProcessor {
    private final PackagingOrchestrationService packagingOrchestrationService;

    public PackagingController(PackagingOrchestrationService packagingOrchestrationService) {
        this.packagingOrchestrationService = packagingOrchestrationService;
    }

    @Operation(summary = "Minimum Package of FHIR Measure for specific measure Id",
            description = "Minimum Packaging of Measure in json or xml format")
    @GetMapping("/measureMinimum")
    public FhirPackage packageMeasureMinimum(@RequestParam String id, @RequestParam String format) {
        return packagingOrchestrationService.packageMinimum(id, format);
    }

    @Operation(summary = "Measure Packaging for Bonnie, include measure, primary library, and included libraries",
            description = "Measure Packaging for Bonnie in either json or xml specified format")
    @GetMapping("/measureForBonnie")
    public FhirPackage packageMeasureForBonnie(@RequestParam String id, String format) {
        return packagingOrchestrationService.packageForBonnie(id, format);
    }

    @Operation(summary = "Measure Packaging Complete for given a measure Id in json format only.",
            description = "Creates Measure Distribution files and directories for later use, at this time codeSystems " +
                    "and valueSets are not included.")
    @GetMapping("/measureFull")
    public FhirPackage packageMeasureFull(@RequestParam String id) {
        //for when ever they do collections
        return packagingOrchestrationService.packageFull(id);
    }

    @Operation(summary = "Returns HumanReadible for given a measure Id",
            description = "Creates Measure Distribution files and directories for later use, at this time codeSystems " +
                    "and valueSets are not included")
    @GetMapping("/humanReadible")
    public String getHumanReadible(@RequestParam String id) {
        return packagingOrchestrationService.getHumanReadible(id);
    }


    @Operation(summary = "Returns Zip File of Measure Distribution for given a measure Id",
            description = "Zip contents of Measures Full distribution and delivers to requester")
    @GetMapping(path = "/measureForDistribution", produces = "application/zip")
    public byte[] packageMeasureForDistribution(@RequestParam String id) {
        return packagingOrchestrationService.packageForDistribution(id);
    }
}
