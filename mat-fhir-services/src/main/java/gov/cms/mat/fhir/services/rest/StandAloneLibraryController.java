package gov.cms.mat.fhir.services.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.FhirConversionHistory;
import gov.cms.mat.fhir.rest.dto.ConversionResultDto;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.reporting.ConversionReporter;
import gov.cms.mat.fhir.services.components.reporting.ConversionResultsService;
import gov.cms.mat.fhir.services.components.reporting.ThreadSessionKey;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.repository.FhirConversionHistoryRepository;
import gov.cms.mat.fhir.services.service.orchestration.PushLibraryService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping(path = "/library")
@Tag(name = "Library-Controller", description = "API for Libraries")
@Slf4j
public class StandAloneLibraryController {
    @Getter
    @Setter
    public static class PushAllResult {
        private List<String> successes = new ArrayList<>();
        private List<String> failures = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class ConvertFhirLibsResult {
        private Map<String, List<String>> successSetIdToFhirLib = new HashMap<String, List<String>>();
    }


    private final ConversionResultsService conversionResultsService;
    private final PushLibraryService pushLibraryService;
    private final CqlLibraryRepository cqlLibraryRepository;
    private final FhirConversionHistoryRepository fhirConversionHistoryRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StandAloneLibraryController(ConversionResultsService conversionResultsService,
                                       PushLibraryService pushLibraryService,
                                       CqlLibraryRepository cqlLibraryRepository, FhirConversionHistoryRepository fhirConversionHistoryRepository) {
        this.conversionResultsService = conversionResultsService;
        this.pushLibraryService = pushLibraryService;
        this.cqlLibraryRepository = cqlLibraryRepository;
        this.fhirConversionHistoryRepository = fhirConversionHistoryRepository;
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

    /**
     * Remove me after FHIR 6.04 release.
     * @return
     */
    @Operation(summary = "For 6.04 release, converts libs to new set id. Not usable after 6.04 release")
    @GetMapping("/dbConversionForFhirConversionIn6dot04")
    public @ResponseBody ConvertFhirLibsResult dbConversionForFhirConversionIn6dot04() {
        ConvertFhirLibsResult result = new ConvertFhirLibsResult();
        var libIds = cqlLibraryRepository.getAllStandaloneCqlFhirLibs();

        //Place in a map by qdm set id.
        var mapBySetId = new HashMap<String, List<CqlLibrary>>();
        libIds.forEach(l -> {
            List<CqlLibrary> libs = mapBySetId.get(l.getSetId());
            if (libs == null) {
                libs = new ArrayList<>();
                mapBySetId.put(l.getSetId(), libs);
            }
            libs.add(l);
        });

        mapBySetId.forEach((k, v) -> {
            String newSetId = UUID.randomUUID().toString();
            //Create new FhirConversionHistory.
            var h = new FhirConversionHistory();
            h.setFhirSetId(newSetId);
            h.setQdmSetId(k);
            h.setLastModifiedOn(new Timestamp(System.currentTimeMillis()));
            fhirConversionHistoryRepository.save(h);

            //Change to new fhir set id.
            v.forEach(l -> {
                l.setSetId(newSetId);
                cqlLibraryRepository.save(l);
                addToListMap(result.getSuccessSetIdToFhirLib(), newSetId, l.getId());
            });
        });

        return result;
    }

    private void addToListMap(Map<String, List<String>> map, String key, String value) {
        var valueList = map.get(key);
        if (valueList == null) {
            valueList = new ArrayList<>();
            map.put(key, valueList);
        }
        valueList.add(value);
    }

    @Operation(summary = "Pushes all versioned fhir libs in the mat DB into the hapi fhir db.",
            description = "Pushes all versioned fhir libs in the mat DB into the hapi fhir db. Returns a list of lib , names, and versions and the order they were pushed.")
    @GetMapping("/pushAllVersionedLibs")
    public @ResponseBody
    PushAllResult pushAllVersionedLibs() {
        var result = new PushAllResult();
        var libIds = cqlLibraryRepository.getAllVersionedCqlFhirLibs();
        libIds.forEach(libId -> {
            //Have to load them one at a time or else the result set is too big to handle by default.
            var lib = cqlLibraryRepository.getCqlLibraryById(libId);
            String name = lib.getId() + " " +
                    lib.getCqlName() + " " +
                    lib.getLibraryModel() + " v" +
                    lib.getMatVersionFormat();
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
}
