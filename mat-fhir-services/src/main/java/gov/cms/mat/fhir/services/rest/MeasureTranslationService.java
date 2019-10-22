/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.services.rest;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.commons.objects.TranslationOutcome;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import gov.cms.mat.fhir.services.repository.MeasureRepository;
import gov.cms.mat.fhir.services.translate.MeasureMapper;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;


/**
 * @author duanedecouteau
 */
@RestController
@RequestMapping(path = "/qdmtofhir")
@Slf4j
public class MeasureTranslationService {
    private final MeasureRepository measureRepo;
    private final MeasureExportRepository exportRepo;

    @Value("fhir.r4.baseurl")
    private String baseURL;

    public MeasureTranslationService(MeasureRepository measureRepository, MeasureExportRepository measureExportRepository) {
        this.measureRepo = measureRepository;
        this.exportRepo = measureExportRepository;
    }

    @GetMapping(path = "/translateMeasure")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public TranslationOutcome translateMeasureById(@QueryParam("id") String id) {
        TranslationOutcome res = new TranslationOutcome();

        try {
            Measure qdmMeasure = measureRepo.getMeasureById(id);
            res.setFhirIdentity("Measure/" + qdmMeasure.getAbbrName());
            MeasureExport qdmExport = exportRepo.getMeasureExportById(id);
            String humanReadible = new String(qdmExport.getHumanReadable());
            MeasureMapper fhirMapper = new MeasureMapper(qdmMeasure, humanReadible);
            org.hl7.fhir.r4.model.Measure fhirMeasure = fhirMapper.translateToFhir();
            Bundle bundle = new Bundle();
            bundle.setType(Bundle.BundleType.TRANSACTION);
            bundle.addEntry().setResource(fhirMeasure)
                    .getRequest()
                    .setUrl("Measure")
                    .setMethod(Bundle.HTTPVerb.POST);

            //create client and post it
            FhirContext ctx = FhirContext.forR4();
            System.out.println(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(bundle));

            // Create a client and post the transaction to the server
            //change this to read from mat.fhir.properties
            IGenericClient client = ctx.newRestfulGenericClient(baseURL);
            Bundle resp = client.transaction().withBundle(bundle).execute();

            // Log the response
            log.info(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(resp));
        } catch (Exception ex) {
            res.setSuccessful(Boolean.FALSE);
            res.setMessage("/qdmtofhir/translateMeasure Failed " + id + " " + ex.getMessage());
            log.error("Failed to Translate Measure: {}", ex.getMessage());
        }
        return res;
    }

//    private void setConfigs() {
//        ConfigUtils config = ConfigUtils.getInstance(ConfigUtilConstants.CONTEXT_NAME);
//        baseURL = config.getString(ConfigUtilConstants.FHIR_BASE_URL);
//    }

}
