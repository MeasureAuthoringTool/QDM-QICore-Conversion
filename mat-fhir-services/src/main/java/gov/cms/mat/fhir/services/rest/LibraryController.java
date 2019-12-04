/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.services.rest;

import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.CqlLibraryExport;
import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.commons.objects.CQLSourceForTranslation;
import gov.cms.mat.fhir.commons.objects.TranslationOutcome;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.repository.CqlLibraryExportRepository;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.repository.MeasureRepository;
import gov.cms.mat.fhir.services.translate.LibraryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping(path = "/library")
@Tag(name = "Library-Controller", description = "API for converting MAT Libraries to FHIR")
@Slf4j
public class LibraryController {
    private final MeasureRepository measureRepo;
    private final HapiFhirServer hapiFhirServer;
    private final CqlLibraryRepository cqlLibraryRepo;
    private final CqlLibraryExportRepository cqlLibraryExportRepo;
    private final ConversionResultsService conversionResultsService;

    public LibraryController(MeasureRepository measureRepository,
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
        ConversionReporter.resetLibrary();

        try {
            List<CqlLibrary> cqlLibs = cqlLibraryRepo.getCqlLibraryByMeasureId(id);
            Iterator iter = cqlLibs.iterator();
            while (iter.hasNext()) {
                CqlLibrary cqlLib = (CqlLibrary) iter.next();
                //go get the associated cql and elm
                CqlLibraryExport cqlExp = cqlLibraryExportRepo.getCqlLibraryExportByCqlLibraryId(cqlLib.getId());
                byte[] cql = cqlExp.getCql();
                byte[] elm = cqlExp.getElm();
                LibraryMapper fhirMapper = new LibraryMapper(cqlLib, cql, elm, hapiFhirServer.getBaseURL());
                org.hl7.fhir.r4.model.Library fhirLibrary = fhirMapper.translateToFhir();

                Bundle bundle = hapiFhirServer.createAndExecuteBundle(fhirLibrary);

                IGenericClient client = hapiFhirServer.getHapiClient();
                Bundle resp = client.transaction().withBundle(bundle).execute();

                // Log the response
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

    @Operation(summary = "Find a list of CQLSourceForTranslation.",
            description = "Find a list of CQLSourceForTranslation identified by the id.")
    @GetMapping(path = "/getLibrariesByMeasureId")
    public List<CQLSourceForTranslation> getLibrariesByMeasureId(@RequestParam("id") String id) {
        List<CQLSourceForTranslation> res = new ArrayList<>();
        try {
            List<CqlLibrary> libraries = cqlLibraryRepo.getCqlLibraryByMeasureId(id);
            Iterator iter = libraries.iterator();
            while (iter.hasNext()) {
                CqlLibrary lib = (CqlLibrary) iter.next();
                CQLSourceForTranslation dest = new CQLSourceForTranslation();
                dest.setId(lib.getId());
                dest.setMeasureId(lib.getMeasureId());
                dest.setCql(Base64.getEncoder().encodeToString(lib.getCqlXml().getBytes()));
                dest.setQdmVersion(lib.getQdmVersion());
                dest.setReleaseVersion(lib.getReleaseVersion());
                res.add(dest);
            }
        } catch (Exception ex) {
            log.error("Error in Library search for this measure: {}", ex.getMessage());
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
            log.error("Error in Library search for this measure: {}", ex.getMessage());
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
            log.error("Error in Library search for this measure: {}", ex.getMessage());
        }
        return dest;
    }


    @Operation(summary = "Translate all Libraries in MAT to FHIR.",
            description = "Translate all the Libraries in the MAT Database and persist to the HAPI FHIR Database.")
    @PutMapping(path = "/translateAllLibraries")
    public List<TranslationOutcome> translateAllLibraries() {
        List<TranslationOutcome> res = new ArrayList<>();
        try {
            List<Measure> measureList = measureRepo.findAll();
            Iterator iter = measureList.iterator();
            while (iter.hasNext()) {
                Measure measure = (Measure) iter.next();
                String measureId = measure.getId().trim();
                String version = measure.getReleaseVersion();

                if (version != null) {
                    if (version.equals("v5.5") || version.equals("v5.6") || version.equals("v5.7") || version.equals("v5.8")) {
                        System.out.println("Translating Libraries for " + measureId);
                        TranslationOutcome result = translateLibraryByMeasureId(measureId);
                        res.add(result);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            TranslationOutcome tOut = new TranslationOutcome();
            tOut.setSuccessful(Boolean.FALSE);
            tOut.setMessage("/library/translateAllLibraries Failed " + ex.getMessage());
            res.add(tOut);
            log.error("Failed Batch Translation of Libraries ALL: {}", ex.getMessage());
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
            Iterator iter = exportList.iterator();
            while (iter.hasNext()) {
                CqlLibraryExport library = (CqlLibraryExport) iter.next();
                String cqlId = library.getCqlLibraryId();
                try {
                    IGenericClient client = hapiFhirServer.getHapiClient();
                    client.delete().resourceById(new IdDt("Library", cqlId)).execute();
                } catch (Exception ex) {
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            res.setSuccessful(Boolean.FALSE);
            res.setMessage("/library/removeAllLibraries removeAllLibraries Failed " + ex.getMessage());
            log.error("Failed Batch Delete of Libraries ALL: {}", ex.getMessage());
        }

        return res;
    }
}
