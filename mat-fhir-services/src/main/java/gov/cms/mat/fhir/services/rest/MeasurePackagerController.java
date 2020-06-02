package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.packaging.MeasurePackagerService;
import gov.cms.mat.fhir.services.service.packaging.dto.PackageFormat;
import gov.cms.mat.fhir.services.service.packaging.dto.MeasurePackageFullData;
import gov.cms.mat.fhir.services.service.packaging.dto.MeasurePackageFullHapi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
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
            description = "Full Packaging of Measure in json")
    @RequestMapping(path = "/measure/package")
    @GetMapping
    @ResponseBody
    public MeasurePackageFullData packageFullJson(@RequestParam String id) {
        MeasurePackageFullHapi fullHapi = measurePackagerService.packageFull(id);

        return MeasurePackageFullData.builder()
                .measure(hapiFhirServer.toJson(fullHapi.getMeasure()))
                .library(hapiFhirServer.toJson(fullHapi.getLibrary()))
                .includeBundle(hapiFhirServer.toJson(fullHapi.getIncludeBundle()))
                .build();
    }
}
