package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.rest.dto.ConversionResultDto;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.reporting.ConversionReporter;
import gov.cms.mat.fhir.services.components.reporting.ConversionResultsService;
import gov.cms.mat.fhir.services.components.reporting.ThreadSessionKey;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.service.orchestration.PushLibraryService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/library")
@Tag(name = "Library-Controller", description = "API for Libraries")
@Slf4j
public class StandAloneLibraryController {
    private final ConversionResultsService conversionResultsService;
    private final PushLibraryService pushLibraryService;
    private final CqlLibraryRepository cqlLibraryRepository;

    public StandAloneLibraryController(ConversionResultsService conversionResultsService,
                                       PushLibraryService pushLibraryService,
                                       CqlLibraryRepository cqlLibraryRepository) {
        this.conversionResultsService = conversionResultsService;
        this.pushLibraryService = pushLibraryService;
        this.cqlLibraryRepository = cqlLibraryRepository;
    }

    @Operation(summary = "Orchestrate QDM to Hapi FHIR Library with the id",
            description = "Find the CQL QDM Library and convert and persist to FHIR",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Library found and results returnd"),
                    @ApiResponse(responseCode = "404", description = "CqlLibrary is not found in the mat db using the id")})
    @PostMapping("/convertStandAlone")
    public ConversionResultDto convertQdmToFhir(
            @RequestParam @Min(10) String id,
            @RequestParam(required = false) ConversionType conversionType,
            @RequestParam(required = false, defaultValue = "false") boolean showWarnings,
            @RequestParam(required = false, defaultValue = "LIBRARY-QDM-ORCHESTRATION") String batchId) {

        try {
            ThreadSessionKey threadSessionKey = buildThreadSessionKey(id, conversionType, showWarnings, batchId);
            OrchestrationProperties orchestrationProperties =
                    buildProperties(conversionType, false, showWarnings, threadSessionKey);
            return pushLibraryService.convertQdmToFhir(id, orchestrationProperties);
        } catch (RuntimeException r) {
            log.error("convertQdmToFhir", r);
            throw r;
        }
    }

    @Operation(summary = "Orchestrate Stand alone Hapi FHIR Library with the id",
            description = "Find the HAPI stand alone Library and persist to FHIR",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Value set found and json returned"),
                    @ApiResponse(responseCode = "404", description = "CqlLibrary is not found in the mat db using the id")})
    @PostMapping("/pushStandAloneLibrary")
    public String pushStandAloneFromMatToFhir(
            @RequestParam @Min(10) String id,
            @RequestParam(required = false, defaultValue = "LIBRARY-STANDALONE-ORCHESTRATION") String batchId) {
        try {
            ThreadSessionKey threadSessionKey = buildThreadSessionKey(id, ConversionType.CONVERSION, Boolean.FALSE, batchId);
            OrchestrationProperties orchestrationProperties =
                    buildProperties(ConversionType.CONVERSION, true, Boolean.FALSE, threadSessionKey);
            return pushLibraryService.convertStandAloneFromMatToFhir(id, orchestrationProperties);
        } catch (RuntimeException r) {
            log.error("pushStandAloneLibrary", r);
            throw r;
        }
    }

    @Operation(summary = "Pushes all versioned fhir libs in the mat DB into the hapi fhir db.",
            description = "Pushes all versioned fhir libs in the mat DB into the hapi fhir db. Returns a list of lib , names, and versions and the order they were pushed.")
    @GetMapping("/pushAllVersionedLibs")
    public @ResponseBody List<String> pushAllVersionedLibs() {
        try {
            var libs = cqlLibraryRepository.getAllVersionedCqlFhirLibs();
            var result = new ArrayList<String>();
            libs.forEach(lib -> result.add(lib.getId() + " " +
                    lib.getCqlName() + " " +
                    lib.getLibraryModel() + " v" +
                    lib.getMatVersionFormat()));
            log.info("Pushing the following libs to hapi-fhir db: " + libs);
            libs.forEach(lib -> pushStandAloneFromMatToFhir(lib.getId(),null));
            return result;
        } catch (RuntimeException r) {
            log.error("getVersionedLibIds", r);
            throw r;
        }
    }

    public OrchestrationProperties buildProperties(ConversionType conversionType,
                                                   boolean isPush,
                                                   boolean showWarnings,
                                                   ThreadSessionKey threadSessionKey) {
        return OrchestrationProperties.builder()
                .showWarnings(showWarnings)
                .conversionType(conversionType)
                .isPush(isPush)
                .includeStdLibs(false)
                .xmlSource(XmlSource.MEASURE)
                .threadSessionKey(threadSessionKey)
                .build();
    }

    public ThreadSessionKey buildThreadSessionKey(String id,
                                                  ConversionType conversionType,
                                                  boolean showWarnings,
                                                  String batchId) {
        return ConversionReporter.setInThreadLocal("L-" + id,
                batchId,
                conversionResultsService,
                Instant.now(),
                conversionType,
                XmlSource.MEASURE,
                showWarnings,
                "");
    }
}
