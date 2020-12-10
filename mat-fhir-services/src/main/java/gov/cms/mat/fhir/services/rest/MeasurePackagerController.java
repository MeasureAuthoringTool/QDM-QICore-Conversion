package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.HumanReadableArtifacts;
import gov.cms.mat.fhir.services.cql.CQLAntlrUtils;
import gov.cms.mat.fhir.services.cql.LibraryCqlVisitorFactory;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.service.packaging.MeasurePackagerService;
import gov.cms.mat.fhir.services.service.packaging.dto.MeasurePackageFullData;
import gov.cms.mat.fhir.services.service.packaging.dto.MeasurePackageFullHapi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Tag(name = "Measure-Controller", description = "API for packaging measures")
@Slf4j
public class MeasurePackagerController {
    private final MeasurePackagerService measurePackagerService;
    private final HapiFhirServer hapiFhirServer;
    private final CqlLibraryRepository cqlLibRepository;
    private final LibraryCqlVisitorFactory cqlVisitorFactory;
    private final CQLAntlrUtils cqlAntlrUtils;

    public MeasurePackagerController(MeasurePackagerService measurePackagerService,
                                     HapiFhirServer hapiFhirServer,
                                     CqlLibraryRepository cqlLibRepository,
                                     LibraryCqlVisitorFactory cqlVisitorFactory,
                                     CQLAntlrUtils cqlAntlrUtils) {
        this.measurePackagerService = measurePackagerService;
        this.hapiFhirServer = hapiFhirServer;
        this.cqlLibRepository = cqlLibRepository;
        this.cqlVisitorFactory = cqlVisitorFactory;
        this.cqlAntlrUtils = cqlAntlrUtils;
    }

    @Operation(summary = "Full json Package of FHIR Measure",
            description = "Full Packaging of Measure in json")
    @RequestMapping(path = "/measure/package")
    @GetMapping
    @ResponseBody
    public MeasurePackageFullData packageFullJson(@RequestParam String id) {
        try {
        MeasurePackageFullHapi fullHapi = measurePackagerService.packageFull(id);

        return MeasurePackageFullData.builder()
                .measure(hapiFhirServer.toJson(fullHapi.getMeasure()))
                .library(hapiFhirServer.toJson(fullHapi.getLibrary()))
                .includeBundle(hapiFhirServer.toJson(fullHapi.getIncludeBundle()))
                .build();
        } catch (RuntimeException r) {
            log.error("getHumanReadableArtifacts",r);
            throw r;
        }
    }

    @Operation(summary = "Returns human readable artifacts for a packaged measure.",
            description = "Returns human readable artifacts for a packaged measure.")
    @RequestMapping(path = "/measure/package/humanReadableArtifacts/{measureId}")
    @GetMapping
    @ResponseBody
    public HumanReadableArtifacts getHumanReadableArtifacts(@PathVariable("measureId") String measureId) {
        try {
            CqlLibrary matLib = cqlLibRepository.getCqlLibraryByMeasureId(measureId);
            if (matLib != null) {
                var lib = hapiFhirServer.fetchHapiLibrary(matLib.getId()).
                        orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Could not find Hapi Fhir Library " + matLib.getId()));
                return cqlVisitorFactory.visitAndCollateHumanReadable(cqlAntlrUtils.getCql(lib)).getRight();
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find measure lib in MAT DB for measureId " + measureId);
            }
        } catch (RuntimeException r) {
            log.error("getHumanReadableArtifacts",r);
            throw r;
        }
    }
}
