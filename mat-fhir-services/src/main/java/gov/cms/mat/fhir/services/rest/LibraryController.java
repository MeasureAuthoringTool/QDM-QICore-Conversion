package gov.cms.mat.fhir.services.rest;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.CqlLibraryExport;
import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.commons.objects.CQLSourceForTranslation;
import gov.cms.mat.fhir.commons.objects.FhirResourceValidationError;
import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.commons.objects.TranslationOutcome;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResult;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.components.mongo.ConversionType;
import gov.cms.mat.fhir.services.exceptions.LibraryConversionException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.repository.CqlLibraryExportRepository;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.service.MeasureService;
import gov.cms.mat.fhir.services.summary.FhirLibraryResourceValidationResult;
import gov.cms.mat.fhir.services.translate.LibraryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping(path = "/library")
@Tag(name = "Library-Controller", description = "API for converting MAT Libraries to FHIR")
@Slf4j
public class LibraryController {
    private static final String ERROR_LIBRARY_SEARCH = "Error in Library search for this measure: {}";

    private final MeasureService measureRepo;
    private final HapiFhirServer hapiFhirServer;
    private final CqlLibraryRepository cqlLibraryRepo;
    private final CqlLibraryExportRepository cqlLibraryExportRepo;
    private final ConversionResultsService conversionResultsService;

    public LibraryController(MeasureService measureRepository,
                             HapiFhirServer hapiFhirServer,
                             CqlLibraryRepository cqlLibraryRepo,
                             CqlLibraryExportRepository cqlLibraryExportRepo,
                             ConversionResultsService conversionResultsService) {
        this.measureRepo = measureRepository;
        this.hapiFhirServer = hapiFhirServer;
        this.cqlLibraryExportRepo = cqlLibraryExportRepo;
        this.cqlLibraryRepo = cqlLibraryRepo;
        this.conversionResultsService = conversionResultsService;
    }


    @Operation(summary = "Translate Library in MAT to FHIR.",
            description = "Translate one Library in the MAT Database and persist to the HAPI FHIR Database.")
    @PutMapping(path = "/translateLibraryByMeasureId")
    public TranslationOutcome translateLibraryByMeasureId(@RequestParam("id") String id) {
        TranslationOutcome res = new TranslationOutcome();
        ConversionReporter.setInThreadLocal(id, conversionResultsService);
        ConversionReporter.resetLibrary(ConversionType.CONVERSION);

        try {
            List<CqlLibrary> cqlLibs = cqlLibraryRepo.getCqlLibraryByMeasureId(id);

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
    public FhirResourceValidationResult validateLibraryByMeasureId(@RequestParam("id") String id) {

        try {
            FhirLibraryResourceValidationResult res = new FhirLibraryResourceValidationResult();

            ConversionReporter.setInThreadLocal(id, conversionResultsService);
            ConversionReporter.resetLibrary(ConversionType.VALIDATION);

            List<CqlLibrary> cqlLibs = cqlLibraryRepo.getCqlLibraryByMeasureId(id);

            for (CqlLibrary cqlLib : cqlLibs) {
                Library fhirLibrary = translateLibrary(cqlLib);
                validateResource(res, fhirLibrary);

                res.setId(id);
                res.setType("Library");

                ConversionResult conversionResult = ConversionReporter.getConversionResult();
                res.setLibraryResults(conversionResult.getLibraryResults());
                res.setLibraryConversionType(conversionResult.getLibraryConversionType());
            }

            return res;

        } catch (Exception ex) {
            String message = "Failed to Translate Library for measureId: " + id;
            log.error(message, ex);
            throw new LibraryConversionException(message, ex);
        }

    }

    public void validateResource(FhirResourceValidationResult res, IBaseResource resource) {
        //validate the Measure Resource
        FhirContext ctx = hapiFhirServer.getCtx();

        FhirValidator validator = ctx.newValidator();
        FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
        validator.registerValidatorModule(instanceValidator);

        ValidationResult result = validator.validateWithResult(resource);

        for (SingleValidationMessage next : result.getMessages()) {
            FhirResourceValidationError error = new FhirResourceValidationError(next.getSeverity().name(), next.getLocationString(), next.getMessage());
            res.getErrorList().add(error);
        }
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
    public List<CQLSourceForTranslation> getLibrariesByMeasureId(@RequestParam("id") String id) {
        List<CQLSourceForTranslation> res = new ArrayList<>();
        try {
            List<CqlLibrary> libraries = cqlLibraryRepo.getCqlLibraryByMeasureId(id);

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
    public CQLSourceForTranslation getLibraryById(@RequestParam("id") String id) {
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
    public CQLSourceForTranslation getLibraryByNameAndVersion(@RequestParam("cqlName") String cqlName, @RequestParam("version") String version) {
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
            List<Measure> measureList = measureRepo.findAllValid();

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
