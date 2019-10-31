/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.services.rest;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.commons.model.MeasureDetails;
import gov.cms.mat.fhir.commons.model.MeasureDetailsReference;
import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.commons.objects.TranslationOutcome;
import gov.cms.mat.fhir.services.repository.MeasureDetailsReferenceRepository;
import gov.cms.mat.fhir.services.repository.MeasureDetailsRepository;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import gov.cms.mat.fhir.services.repository.MeasureRepository;
import gov.cms.mat.fhir.services.translate.ManageMeasureDetailMapper;
import gov.cms.mat.fhir.services.translate.MeasureMapper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import mat.client.measure.ManageCompositeMeasureDetailModel;


/**
 * @author duanedecouteau
 */
@RestController
@RequestMapping(path = "/qdmtofhir")
@Slf4j
public class MeasureTranslationService {
    private final MeasureRepository measureRepo;
    private final MeasureDetailsRepository measureDetailsRepo;
    private final MeasureDetailsReferenceRepository measureDetailsReferenceRepo;
    private final MeasureExportRepository measureExportRepo;


    @Value("${fhir.r4.baseurl}")
    private String baseURL = "http://localhost:8080/hapi-fhir-jpaserver/fhir/";

    public MeasureTranslationService(MeasureRepository measureRepository, MeasureDetailsRepository measureDetailsRepository, MeasureDetailsReferenceRepository measureDetailsReferenceRepository, MeasureExportRepository measureExportRepository) {
        this.measureRepo = measureRepository;
        this.measureDetailsRepo = measureDetailsRepository;
        this.measureDetailsReferenceRepo = measureDetailsReferenceRepository;
        this.measureExportRepo = measureExportRepository;
    }

    @GetMapping(path = "/translateMeasure")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public TranslationOutcome translateMeasureById(@QueryParam("id") String id) {
        TranslationOutcome res = new TranslationOutcome();

        try {
            Measure qdmMeasure = measureRepo.getMeasureById(id);
            res.setFhirIdentity("Measure/" + qdmMeasure.getId());
//            Move to ManageMeasureDetailModel            
//            MeasureDetails qdmMeasureDetails = measureDetailsRepo.getMeasureDetailsByMeasureId(id);
//            Integer detailsId = qdmMeasureDetails.getId();
//            System.out.println("Measure Details ID "+ detailsId);
//            List<MeasureDetailsReference> qdmMeasureReferenceList = measureDetailsReferenceRepo.getMeasureDetailsReferenceByMeasureDetailsId(detailsId);
            MeasureExport measureExport = measureExportRepo.getMeasureExportById(id);
            byte[] xmlBytes = measureExport.getSimpleXml();
            //humanreadible may exist not an error if it doesn't
            String narrative = "";
            try {
                narrative = new String(measureExport.getHqmf());
            }
            catch (Exception ex) {
                log.error("Narrative not found", ex.getMessage());
            }
            
            ManageMeasureDetailMapper manageMeasureDetailMapper = new ManageMeasureDetailMapper(xmlBytes, qdmMeasure);

            ManageCompositeMeasureDetailModel model = manageMeasureDetailMapper.convert();
            
            
            MeasureMapper fhirMapper = new MeasureMapper(model, narrative);
            org.hl7.fhir.r4.model.Measure fhirMeasure = fhirMapper.translateToFhir();
            Bundle bundle = new Bundle();
            bundle.setType(Bundle.BundleType.TRANSACTION);
            bundle.addEntry().setResource(fhirMeasure)
                    .getRequest()
                    .setUrl(baseURL+"Measure/"+qdmMeasure.getId())
                    .setMethod(Bundle.HTTPVerb.PUT);

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
            ex.printStackTrace();
            res.setSuccessful(Boolean.FALSE);
            res.setMessage("/qdmtofhir/translateMeasure Failed " + id + " " + ex.getMessage());
            log.error("Failed to Translate Measure: {}", ex.getMessage());
        }
        return res;
    }
    
    
    @GetMapping(path = "/translateMeasuresByStatus")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<TranslationOutcome> translateMeasuresByStatus(@QueryParam("measureStatus") String measureStatus) {
        List<TranslationOutcome> res = new ArrayList();
        try {
            List<Measure> measureList = measureRepo.getMeasuresByStatus(measureStatus);
            measureRepo.flush();
            Iterator iter = measureList.iterator();
            while (iter.hasNext()) {
                Measure measure = (Measure)iter.next();
                String measureId = measure.getId().trim();
                System.out.println("Translating Measure "+measureId);
                TranslationOutcome result = translateMeasureById(measureId);
                res.add(result);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            TranslationOutcome tOut = new TranslationOutcome();
            tOut.setSuccessful(Boolean.FALSE);
            tOut.setMessage("/qdmtofhir/translateMeasuresByStatus Failed " + ex.getMessage());
            res.add(tOut);
            log.error("Failed Batch Translation of Measures: {}", ex.getMessage());
        }
        
        return res;
    }
    
    @GetMapping(path = "/translateAllMeasures")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<TranslationOutcome> translateAllMeasures() {
        List<TranslationOutcome> res = new ArrayList();
        try {
            List<Measure> measureList = measureRepo.findAll();
            Iterator iter = measureList.iterator();
            while (iter.hasNext()) {
                Measure measure = (Measure)iter.next();
                String measureId = measure.getId().trim();
                System.out.println("Translating Measure "+measureId);
                TranslationOutcome result = translateMeasureById(measureId);
                res.add(result);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            TranslationOutcome tOut = new TranslationOutcome();
            tOut.setSuccessful(Boolean.FALSE);
            tOut.setMessage("/qdmtofhir/translateAllMeasures Failed " + ex.getMessage());
            res.add(tOut);
            log.error("Failed Batch Translation of Measures ALL: {}", ex.getMessage());
        }
        
        return res;
    }
    
    @GetMapping(path = "/removeAllMeasures")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public TranslationOutcome removeAllMeasures() {
        TranslationOutcome res = new TranslationOutcome();
        try {
            List<Measure> measureList = measureRepo.findAll();
            Iterator iter = measureList.iterator();
            while (iter.hasNext()) {
                Measure measure = (Measure)iter.next();
                String measureId = measure.getId().trim();
                System.out.println("Removing Measure "+measureId);
                
                
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            res.setSuccessful(Boolean.FALSE);
            res.setMessage("/qdmtofhir removeAllMeasures Failed " + ex.getMessage());
            log.error("Failed Batch Translation of Measures ALL: {}", ex.getMessage());
        }
        
        return res;
    }
    


//    private void setConfigs() {
//        ConfigUtils config = ConfigUtils.getInstance(ConfigUtilConstants.CONTEXT_NAME);
//        baseURL = config.getString(ConfigUtilConstants.FHIR_BASE_URL);
//    }

}
