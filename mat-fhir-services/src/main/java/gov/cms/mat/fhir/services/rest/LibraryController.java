package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.CqlLibraryExport;
import gov.cms.mat.fhir.commons.objects.CQLSourceForTranslation;
import gov.cms.mat.fhir.rest.dto.FhirValidationResult;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResult;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.exceptions.LibraryConversionException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.repository.CqlLibraryExportRepository;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.service.CqlLibraryDataService;
import gov.cms.mat.fhir.services.service.MeasureDataService;
import gov.cms.mat.fhir.services.summary.FhirLibraryResourceValidationResult;
import gov.cms.mat.fhir.services.translate.LibraryMapper;
import gov.cms.mat.fhir.services.translate.LibraryTranslator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Library;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/library")
@Tag(name = "Library-Controller", description = "API for converting MAT Libraries to FHIR")
@Slf4j
public class LibraryController implements FhirValidatorProcessor {
    private static final String ERROR_LIBRARY_SEARCH = "Error in Library search for this measure: {}";

    private final MeasureDataService measureDataService;
    private final HapiFhirServer hapiFhirServer;
    private final CqlLibraryDataService cqlLibraryDataService;
    private final CqlLibraryExportRepository cqlLibraryExportRepo;
    private final ConversionResultsService conversionResultsService;

    private final LibraryMapper libraryMapper;

    public LibraryController(MeasureDataService measureDataService,
                             HapiFhirServer hapiFhirServer,
                             CqlLibraryDataService cqlLibraryDataService,
                             CqlLibraryExportRepository cqlLibraryExportRepo,
                             ConversionResultsService conversionResultsService,
                             LibraryMapper libraryMapper) {
        this.measureDataService = measureDataService;
        this.hapiFhirServer = hapiFhirServer;
        this.cqlLibraryExportRepo = cqlLibraryExportRepo;
        this.cqlLibraryDataService = cqlLibraryDataService;
        this.conversionResultsService = conversionResultsService;
        this.libraryMapper = libraryMapper;
    }




    @Operation(summary = "Validate Library in MAT to FHIR.",
            description = "Translate one Library in the MAT Database and persist to the HAPI FHIR Database.")
    @PutMapping(path = "/validateLibraryByMeasureId")
    public List<FhirLibraryResourceValidationResult> validateLibraryByMeasureId(@RequestParam String id) {
        try {
            List<CqlLibrary> cqlLibs = cqlLibraryDataService.getCqlLibrariesByMeasureIdRequired(id);

            ConversionReporter.setInThreadLocal(id, conversionResultsService, Instant.now());

            return cqlLibs.stream()
                    .map(this::validate)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            String message = "Failed to Translate Library for measureId: " + id;
            log.error(message, e);
            throw new LibraryConversionException(message, e);
        }
    }

    private FhirLibraryResourceValidationResult validate(CqlLibrary matCqlLibrary) {
        FhirLibraryResourceValidationResult response = new FhirLibraryResourceValidationResult(matCqlLibrary.getId());
        response.setMeasureId(matCqlLibrary.getMeasureId());

        Library fhirLibrary = translateLibrary(matCqlLibrary);
        validateResource(response, fhirLibrary, hapiFhirServer.getCtx());

        List<FhirValidationResult> list = buildResults(response);
        ConversionReporter.setFhirLibraryValidationResults(list, matCqlLibrary.getId());

        ConversionResult conversionResult = ConversionReporter.getConversionResult();
        conversionResult.findOrCreateLibraryConversionResults(matCqlLibrary.getId());

        response.setLibraryConversionResults(conversionResult.getLibraryConversionResults());
        response.setLibraryConversionType(conversionResult.getConversionType());

        return response;
    }

    public Library translateLibrary(CqlLibrary cqlLib) {
        //go get the associated cql and elm
        CqlLibraryExport cqlExp = cqlLibraryExportRepo.getCqlLibraryExportByCqlLibraryId(cqlLib.getId());
        byte[] cql = cqlExp.getCql();
        byte[] elm = cqlExp.getElm();
        LibraryTranslator fhirMapperTranslator = new LibraryTranslator(cqlLib, cql, elm, hapiFhirServer.getBaseURL());
        return fhirMapperTranslator.translateToFhir();
    }

    @Operation(summary = "Find a list of CQLSourceForTranslation.",
            description = "Find a list of CQLSourceForTranslation identified by the id.")
    @GetMapping(path = "/getLibrariesByMeasureId")
    public List<CQLSourceForTranslation> getLibrariesByMeasureId(@RequestParam String id) {
        List<CQLSourceForTranslation> res = new ArrayList<>();
        try {
            List<CqlLibrary> libraries = cqlLibraryDataService.getCqlLibrariesByMeasureIdRequired(id);

            for (CqlLibrary lib : libraries) {
                CQLSourceForTranslation dest = new CQLSourceForTranslation();
                dest.setId(lib.getId());
                dest.setMeasureId(lib.getMeasureId());
                dest.setCql(Base64.getEncoder().encodeToString(lib.getCqlXml().getBytes()));
                dest.setQdmVersion(lib.getQdmVersion());
                dest.setReleaseVersion(lib.getReleaseVersion());
                res.add(dest);
            }
        } catch (Exception ex) {
            log.error(ERROR_LIBRARY_SEARCH, ex.getMessage());
        }
        return res;
    }

    @Operation(summary = "Find a CQLSourceForTranslation.",
            description = "Find a CQLSourceForTranslation identified by the id.")
    @GetMapping(path = "/getLibraryById")
    public CQLSourceForTranslation getLibraryById(@RequestParam String id) {
        CQLSourceForTranslation dest = new CQLSourceForTranslation();
        try {
            CqlLibrary lib = cqlLibraryDataService.getCqlLibraryById(id);
            dest.setId(lib.getId());
            dest.setMeasureId(lib.getMeasureId());
            dest.setCql(Base64.getEncoder().encodeToString(lib.getCqlXml().getBytes()));
            dest.setQdmVersion(lib.getQdmVersion());
            dest.setReleaseVersion(lib.getReleaseVersion());
        } catch (Exception ex) {
            log.error(ERROR_LIBRARY_SEARCH, ex.getMessage());
        }
        return dest;
    }

    @Operation(summary = "Find a CQLSourceForTranslation.",
            description = "Find a CQLSourceForTranslation identified by the name and version.")
    @GetMapping(path = "/getLibraryByNameAndVersion")
    public CQLSourceForTranslation getLibraryByNameAndVersion(@RequestParam String cqlName, @RequestParam String version) {
        CQLSourceForTranslation dest = new CQLSourceForTranslation();
        try {
            CqlLibrary lib = cqlLibraryDataService.getCqlLibraryByNameAndVersion(cqlName, new BigDecimal(version));
            dest.setId(lib.getId());
            dest.setMeasureId(lib.getMeasureId());
            dest.setCql(Base64.getEncoder().encodeToString(lib.getCqlXml().getBytes()));
            dest.setQdmVersion(lib.getQdmVersion());
            dest.setReleaseVersion(lib.getReleaseVersion());
        } catch (Exception ex) {
            log.error(ERROR_LIBRARY_SEARCH, ex.getMessage());
        }
        return dest;
    }

    @Operation(summary = "Count of persisted FHIR ValueSets.",
            description = "The count of all the ValueSets in the HAPI FHIR Database.")
    @GetMapping(path = "/count")
    public int countValueSets() {
        return libraryMapper.count();
    }

    @Operation(summary = "Delete all persisted FHIR L.",
            description = "Delete all the ValueSets in the HAPI FHIR Database. (chiefly used for testing)")
    @DeleteMapping(path = "/deleteAll")
    public int deleteValueSets() {
        return libraryMapper.deleteAll();
    }


}
