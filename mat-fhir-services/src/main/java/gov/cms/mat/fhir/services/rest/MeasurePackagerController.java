package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.packaging.MeasurePackagerService;
import gov.cms.mat.fhir.services.service.packaging.dto.PackageFormat;
import gov.cms.mat.fhir.services.service.packaging.dto.MeasurePackageFullData;
import gov.cms.mat.fhir.services.service.packaging.dto.MeasurePackageFullHapi;
import gov.cms.mat.fhir.services.service.packaging.dto.MeasurePackageMinimumData;
import gov.cms.mat.fhir.services.service.packaging.dto.MeasurePackageMinimumHapi;
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
@RequestMapping(path = "/measure/packager")
@Tag(name = "Measure-Controller", description = "API for packaging measures")
@Slf4j
public class MeasurePackagerController {
    private final MeasurePackagerService measurePackagerService;
    private final HapiFhirServer hapiFhirServer;

    public MeasurePackagerController(MeasurePackagerService measurePackagerService,
                                     HapiFhirServer hapiFhirServer) {
        this.measurePackagerService = measurePackagerService;

        this.hapiFhirServer = hapiFhirServer;
    }

    @Operation(summary = "Full json Package of FHIR Measure",
            description = "Minimum Packaging of Measure in json")
    @GetMapping(value = "/full/json", produces = {"application/json"})
    @ResponseBody
    public MeasurePackageFullData packageFullJson(@RequestParam String id) {
        return packageMeasureFull(id, JSON);
    }

    @Operation(summary = "Full xml Package of FHIR Measure",
            description = "Minimum Packaging of Measure in xml")
    @GetMapping(value = "/full/xml", produces = {"application/json"})
    @ResponseBody
    public MeasurePackageFullData packageFullXml(@RequestParam String id) {
        return packageMeasureFull(id, XML);
    }

    @Operation(summary = "Minimum Package of FHIR Measure",
            description = "Minimum Packaging of Measure in json")
    @GetMapping(value = "/minimum/json", produces = {"application/json"})
    @ResponseBody
    public MeasurePackageMinimumData packageMinimumJson(@RequestParam String id) {
        return packageMeasureMinimum(id, JSON);
    }

    @Operation(summary = "Minimum Package of FHIR Measure",
            description = "Minimum Packaging of Measure in json")
    @GetMapping(value = "/minimum/xml")
    @ResponseBody
    public MeasurePackageMinimumData packageMinimumXML(@RequestParam String id) {
        return packageMeasureMinimum(id, XML);
    }

    private MeasurePackageFullData packageMeasureFull(String id, PackageFormat format) {
        MeasurePackageFullHapi fullHapi = measurePackagerService.packageFull(id);

        return MeasurePackageFullData.builder()
                .measure(hapiFhirServer.formatResource(fullHapi.getMeasure(), format))
                .library(hapiFhirServer.formatResource(fullHapi.getLibrary(), format))
                .includeBundle(hapiFhirServer.formatResource(fullHapi.getIncludeBundle(), format))
                .build();
    }

    private MeasurePackageMinimumData packageMeasureMinimum(String id, PackageFormat format) {
        MeasurePackageMinimumHapi minimumHapi = measurePackagerService.packageMinimum(id);

        return MeasurePackageMinimumData.builder()
                .measure(hapiFhirServer.formatResource(minimumHapi.getMeasure(), format))
                .library(hapiFhirServer.formatResource(minimumHapi.getLibrary(), format))
                .build();
    }
}
