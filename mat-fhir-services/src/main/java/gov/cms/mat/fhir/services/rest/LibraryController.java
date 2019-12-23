package gov.cms.mat.fhir.services.rest;

import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.CqlLibraryExport;
import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.commons.objects.CQLSourceForTranslation;
import gov.cms.mat.fhir.commons.objects.TranslationOutcome;
import gov.cms.mat.fhir.rest.dto.ConversionType;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    private final CqlLibraryDataService cqlLibraryRepo;
    private final CqlLibraryExportRepository cqlLibraryExportRepo;
    private final ConversionResultsService conversionResultsService;

    public LibraryController(MeasureDataService measureDataService,
                             HapiFhirServer hapiFhirServer,
                             CqlLibraryDataService cqlLibraryRepo,
                             CqlLibraryExportRepository cqlLibraryExportRepo,
                             ConversionResultsService conversionResultsService) {
        this.measureDataService = measureDataService;
        this.hapiFhirServer = hapiFhirServer;
        this.cqlLibraryExportRepo = cqlLibraryExportRepo;
        this.cqlLibraryRepo = cqlLibraryRepo;
        this.conversionResultsService = conversionResultsService;
    }


    @Operation(summary = "Translate Library in MAT to FHIR.",
            description = "Translate one Library in the MAT Database and persist to the HAPI FHIR Database.")
    @PutMapping(path = "/translateLibraryByMeasureId")
    public TranslationOutcome translateLibraryByMeasureId(@RequestParam String id) {
        TranslationOutcome res = new TranslationOutcome();
        ConversionReporter.setInThreadLocal(id, conversionResultsService);
        ConversionReporter.resetLibrary(ConversionType.CONVERSION);

        try {
            List<CqlLibrary> cqlLibs = cqlLibraryRepo.getCqlLibrariesByMeasureIdRequired(id);

            for (CqlLibrary cqlLib : cqlLibs) {
                Library fhirLibrary = translateLibrary(cqlLib);

                Bundle bundle = hapiFhirServer.createAndExecuteBundle(fhirLibrary);

                IGenericClient client = hapiFhirServer.getHapiClient();
                Bundle resp = client.transaction().withBundle(bundle).execute();

                log.info(hapiFhirServer.getCtx().newXmlParser().setPrettyPrint(true).encodeResourceToString(resp));
            }
        } catch (Exception ex) {
            res.setSuccessful(Boolean.FALSE);
            if (ex.getMessage() == null) {
                res.setMessage("/library/translateLibrary Failed " + id + " Missing");
            } else {
                res.setMessage("/library/translateLibrary Failed " + id + " " + ex.getMessage());
            }
            log.error("Failed to Translate Library: {}", ex.getMessage());
        }
        return res;
    }


    @Operation(summary = "Validate Library in MAT to FHIR.",
            description = "Translate one Library in the MAT Database and persist to the HAPI FHIR Database.")
    @PutMapping(path = "/validateLibraryByMeasureId")
    public List<FhirLibraryResourceValidationResult> validateLibraryByMeasureId(@RequestParam String id) {
        try {
            List<CqlLibrary> cqlLibs = cqlLibraryRepo.getCqlLibrariesByMeasureIdRequired(id);

            ConversionReporter.setInThreadLocal(id, conversionResultsService);
            ConversionReporter.resetLibrary(ConversionType.VALIDATION);

            return cqlLibs.stream()
                    .map(this::validate)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            String message = "Failed to Translate Library for measureId: " + id;
            log.error(message, e);
            throw new LibraryConversionException(message, e);
        }
    }

    private FhirLibraryResourceValidationResult validate(CqlLibrary cqlLib) {
        FhirLibraryResourceValidationResult response = new FhirLibraryResourceValidationResult(cqlLib.getId());
        response.setMeasureId(cqlLib.getMeasureId());

        Library fhirLibrary = translateLibrary(cqlLib);
        validateResource(response, fhirLibrary, hapiFhirServer.getCtx());

        List<FhirValidationResult> list = buildResults(response);
        ConversionReporter.setFhirLibraryValidationResults(list);


        ConversionResult conversionResult = ConversionReporter.getConversionResult();

        if (conversionResult.getLibraryConversionResults() == null) {
            response.setLibraryResults(conversionResult.getLibraryConversionResults().getLibraryResults());
            response.setLibraryConversionType(conversionResult.getLibraryConversionResults().getLibraryConversionType());
        }

        return response;
    }

    public Library translateLibrary(CqlLibrary cqlLib) {
        //go get the associated cql and elm
        CqlLibraryExport cqlExp = cqlLibraryExportRepo.getCqlLibraryExportByCqlLibraryId(cqlLib.getId());
        byte[] cql = cqlExp.getCql();
        byte[] elm = cqlExp.getElm();
        LibraryMapper fhirMapper = new LibraryMapper(cqlLib, cql, elm, hapiFhirServer.getBaseURL());
        return fhirMapper.translateToFhir();
    }

    @Operation(summary = "Find a list of CQLSourceForTranslation.",
            description = "Find a list of CQLSourceForTranslation identified by the id.")
    @GetMapping(path = "/getLibrariesByMeasureId")
    public List<CQLSourceForTranslation> getLibrariesByMeasureId(@RequestParam String id) {
        List<CQLSourceForTranslation> res = new ArrayList<>();
        try {
            List<CqlLibrary> libraries = cqlLibraryRepo.getCqlLibrariesByMeasureIdRequired(id);

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
            CqlLibrary lib = cqlLibraryRepo.getCqlLibraryById(id);
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
            CqlLibrary lib = cqlLibraryRepo.getCqlLibraryByNameAndVersion(cqlName, new BigDecimal(version));
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


    @Operation(summary = "Translate all Libraries in MAT to FHIR.",
            description = "Translate all the Libraries in the MAT Database and persist to the HAPI FHIR Database.")
    @PutMapping(path = "/translateAllLibraries")
    public List<TranslationOutcome> translateAllLibraries() {
        List<TranslationOutcome> res = new ArrayList<>();
        try {
            List<Measure> measureList = measureDataService.findAllValid();

            for (Measure measure : measureList) {
                String measureId = measure.getId().trim();
                TranslationOutcome result = translateLibraryByMeasureId(measureId);
                res.add(result);
            }
        } catch (Exception ex) {
            TranslationOutcome tOut = new TranslationOutcome();
            tOut.setSuccessful(Boolean.FALSE);
            tOut.setMessage("/library/translateAllLibraries Failed " + ex.getMessage());
            res.add(tOut);
            log.error("Failed Batch Translation of Libraries ALL:", ex);
        }

        ConversionReporter.removeInThreadLocal();
        return res;
    }

    @Operation(summary = "Delete all persisted FHIR Libraries.",
            description = "Delete all the Libraries in the HAPI FHIR Database.")
    @DeleteMapping(path = "/removeAllLibraries")
    public TranslationOutcome removeAllLibraries() {
        TranslationOutcome res = new TranslationOutcome();
        try {
            List<CqlLibraryExport> exportList = cqlLibraryExportRepo.findAll();

            for (CqlLibraryExport library : exportList) {
                deleteLibrary(library.getCqlLibraryId());
            }
        } catch (Exception ex) {
            res.setSuccessful(Boolean.FALSE);
            res.setMessage("/library/removeAllLibraries removeAllLibraries Failed " + ex.getMessage());
            log.error("Failed Batch Delete of Libraries ALL: ", ex);
        }

        return res;
    }

    public void deleteLibrary(String cqlId) {
        try {
            IGenericClient client = hapiFhirServer.getHapiClient();
            client.delete().resourceById(new IdDt("Library", cqlId)).execute();
        } catch (Exception e) {
            log.trace("Error deleting library with cqlId : {}", cqlId, e);
        }
    }
}
