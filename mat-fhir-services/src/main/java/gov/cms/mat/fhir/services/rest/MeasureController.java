package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.FhirConversionHistory;
import gov.cms.mat.fhir.commons.model.MeasureSet;
import gov.cms.mat.fhir.services.exceptions.HapiResourceNotFoundException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.repository.FhirConversionHistoryRepository;
import gov.cms.mat.fhir.services.repository.MeasureRepository;
import gov.cms.mat.fhir.services.repository.MeasureSetRepository;
import gov.cms.mat.fhir.services.translate.MeasureMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Measure;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "/measure")
@Tag(name = "Measure-Controller", description = "API for converting MAT Measures to FHIR")
@Slf4j
public class MeasureController {
    private final HapiFhirServer hapiFhirServer;
    private final MeasureMapper measureMapper;
    private final MeasureRepository measureRepository;
    private final FhirConversionHistoryRepository fhirConversionHistoryRepository;
    private final MeasureSetRepository measureSetRepository;
    private final CqlLibraryRepository cqlLibraryRepository;

    public MeasureController(HapiFhirServer hapiFhirServer,
                             MeasureMapper measureMapper, MeasureRepository measureRepository, FhirConversionHistoryRepository fhirConversionHistoryRepository, MeasureSetRepository measureSetRepository, CqlLibraryRepository cqlLibraryRepository) {
        this.hapiFhirServer = hapiFhirServer;
        this.measureMapper = measureMapper;
        this.measureRepository = measureRepository;
        this.fhirConversionHistoryRepository = fhirConversionHistoryRepository;
        this.measureSetRepository = measureSetRepository;
        this.cqlLibraryRepository = cqlLibraryRepository;
    }

    @Getter
    @Setter
    public static class ConvertFhirMeasureResult {
        private Map<String, List<String>> successSetIdToFhirMeasures = new HashMap<>();
    }

    @Operation(summary = "Find a Hapi FHIR Library with the id",
            description = "Find the Hapi Library converted to json",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Value set found and json returned"),
                    @ApiResponse(responseCode = "404", description = "Measure is not found in the mat db using the id")})
    @GetMapping(path = "/findOne")
    public String findOne(String id) {
        Measure measure = hapiFhirServer.fetchHapiMeasure(id)
                .orElseThrow(() -> new HapiResourceNotFoundException(id, "Measure"));
        return hapiFhirServer.toJson(measure);
    }

    @Operation(summary = "Count of persisted FHIR Measures.",
            description = "The count of all the Measures in the HAPI FHIR Database.")
    @GetMapping(path = "/count")
    public int countMeasures() {
        return measureMapper.count();
    }

    @Operation(summary = "Delete all persisted FHIR Measures.",
            description = "Delete all the Measures in the HAPI FHIR Database.")
    @DeleteMapping(path = "/deleteAll")
    public int deleteMeasures() {
        return measureMapper.deleteAll();
    }

    /**
     * Remove me after FHIR 6.04 release.
     * @return
     */
    @Operation(summary = "For 6.04 release, converts libs to new set id. Not usable after 6.04 release")
    @GetMapping("/dbConversionForFhirConversionIn6dot04")
    public @ResponseBody ConvertFhirMeasureResult dbConversionForFhirConversionIn6dot04() {
        ConvertFhirMeasureResult result = new ConvertFhirMeasureResult();
        var fhirMeasureList = measureRepository.getAllConvertedFhirMeasures();

        //Place in a map by qdm set id.
        var mapBySetId = new HashMap<String, List<gov.cms.mat.fhir.commons.model.Measure>>();
        fhirMeasureList.forEach(m -> {
            List<gov.cms.mat.fhir.commons.model.Measure> measures = mapBySetId.get(m.getMeasureSetId().getId());
            if (measures == null) {
                measures = new ArrayList<>();
                mapBySetId.put(m.getMeasureSetId().getId(), measures);
            }
            measures.add(m);
        });

        mapBySetId.forEach((k, v) -> {
            if (fhirConversionHistoryRepository.findById(k).isEmpty()) {
                String newSetId = UUID.randomUUID().toString();
                //Create new FhirConversionHistory.
                var h = new FhirConversionHistory();
                h.setFhirSetId(newSetId);
                h.setQdmSetId(k);
                h.setLastModifiedOn(new Timestamp(System.currentTimeMillis()));
                fhirConversionHistoryRepository.save(h);

                //Change to new fhir set id.
                v.forEach(measure -> {
                    MeasureSet newMeasureSet = new MeasureSet();
                    newMeasureSet.setId(newSetId);
                    measureSetRepository.save(newMeasureSet);
                    measure.setMeasureSetId(newMeasureSet);
                    measureRepository.save(measure);
                    CqlLibrary cqlLibrary = cqlLibraryRepository.getCqlLibraryByMeasureId(measure.getId());
                    cqlLibrary.setSetId(newSetId);
                    cqlLibraryRepository.save(cqlLibrary);
                    addToListMap(result.getSuccessSetIdToFhirMeasures(), newSetId, measure.getId());
                });
            } else {
                log.info("fhirConversionHistory exists for " + k + " skipping.");
            }
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
}
