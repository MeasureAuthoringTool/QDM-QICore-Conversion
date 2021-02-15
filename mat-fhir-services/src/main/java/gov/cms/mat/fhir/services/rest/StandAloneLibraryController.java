package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.rest.dto.ConversionResultDto;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.reporting.ConversionReporter;
import gov.cms.mat.fhir.services.components.reporting.ConversionResultsService;
import gov.cms.mat.fhir.services.components.reporting.ThreadSessionKey;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.config.ConversionLibraryLookup;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.rest.support.CqlVersionConverter;
import gov.cms.mat.fhir.services.service.orchestration.PushLibraryService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/library")
@Tag(name = "Library-Controller", description = "API for Libraries")
@Slf4j
public class StandAloneLibraryController implements CqlVersionConverter {
    private final ConversionResultsService conversionResultsService;
    private final PushLibraryService pushLibraryService;
    private final CqlLibraryRepository cqlLibraryRepository;
    private final ConversionLibraryLookup conversionLibraryLookup;

    public StandAloneLibraryController(ConversionResultsService conversionResultsService,
                                       PushLibraryService pushLibraryService,
                                       CqlLibraryRepository cqlLibraryRepository,
                                       ConversionLibraryLookup conversionLibraryLookup) {
        this.conversionResultsService = conversionResultsService;
        this.pushLibraryService = pushLibraryService;
        this.cqlLibraryRepository = cqlLibraryRepository;
        this.conversionLibraryLookup = conversionLibraryLookup;
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

        ThreadSessionKey threadSessionKey = buildThreadSessionKey(id, conversionType, showWarnings, batchId);
        OrchestrationProperties orchestrationProperties =
                buildProperties(conversionType, false, showWarnings, threadSessionKey);
        return pushLibraryService.convertQdmToFhir(id, orchestrationProperties);
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

        ThreadSessionKey threadSessionKey = buildThreadSessionKey(id, ConversionType.CONVERSION, Boolean.FALSE, batchId);
        OrchestrationProperties orchestrationProperties =
                buildProperties(ConversionType.CONVERSION, true, Boolean.FALSE, threadSessionKey);
        return pushLibraryService.convertStandAloneFromMatToFhir(id, orchestrationProperties);
    }

    @Operation(summary = "Pushes all versioned fhir libs in the mat DB into the hapi fhir db.",
            description = "Pushes all versioned fhir libs in the mat DB into the hapi fhir db. Returns a list of lib , names, and versions and the order they were pushed.")
    @GetMapping("/pushAllVersionedLibs")
    public @ResponseBody
    PushAllResult pushAllVersionedLibs() {
        var result = new PushAllResult();
        var libIds = buildLibraryIds();

        libIds.forEach(libId -> {
            //Have to load them one at a time or else the result set is too big to handle by default.
            var lib = cqlLibraryRepository.getCqlLibraryById(libId);
            String name = lib.getId() + " " +
                    lib.getCqlName() + " " +
                    lib.getLibraryModel() + " v" +
                    lib.getMatVersionFormat();
            log.debug("VersionedCqlFhirLib name: {} ", name);

            try {
                pushStandAloneFromMatToFhir(lib.getId(), null);
                result.getSuccesses().add(name);
            } catch (RuntimeException rte) {
                log.error("Failed pushing lib to hapi. " + name, rte);
                result.getFailures().add(name);
            }
        });
        return result;
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

    private List<String> buildLibraryIds() {
        List<String> allIds = cqlLibraryRepository.getAllVersionedCqlFhirLibs();

        List<String> stdLibIds = getStandardLibIds();

        List<String> filtered = allIds.stream()
                .filter(s -> !stdLibIds.contains(s))
                .collect(Collectors.toList());

        stdLibIds.addAll(filtered);
        return stdLibIds;
    }

    @NotNull
    private List<String> getStandardLibIds() {
        return conversionLibraryLookup.getMap().entrySet()
                .stream()
                .map(e -> findStandardCqlLibrary(e.getKey(), e.getValue()))
                .filter(Optional::isPresent)
                .map(o -> o.get().getId())
                .collect(Collectors.toList());
    }

    private Optional<CqlLibrary> findStandardCqlLibrary(String cqlName, String versionString) {
        BigDecimal versionDecimal = convertVersionToBigDecimal(versionString);
        String fhirCqlName = convertCqlNameToFhir(cqlName);
        return cqlLibraryRepository.getCqlLibraryByNameAndVersion(fhirCqlName, versionDecimal);
    }

    private String convertCqlNameToFhir(String cqlName) {
        if (cqlName.contains("FHIR")) {
            return cqlName;
        } else {
            return cqlName + "FHIR4";
        }
    }

    @Getter
    @Setter
    public static class PushAllResult {
        private List<String> successes = new ArrayList<>();
        private List<String> failures = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class ConvertFhirLibsResult {
        private Map<String, List<String>> successSetIdToFhirLib = new HashMap<>();
    }
}
