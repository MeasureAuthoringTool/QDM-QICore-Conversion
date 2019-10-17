/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.services.translate;

import java.util.Date;
import java.util.logging.Logger;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Measure;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
/**
 *
 * @author duanedecouteau
 */
public class MeasureMapper {
    public static final Logger LOGGER = Logger.getLogger(MeasureMapper.class.getName());
    private gov.cms.mat.fhir.commons.model.Measure qdmMeasure;
    private String humanReadible;
    
    public MeasureMapper(gov.cms.mat.fhir.commons.model.Measure qdmMeasure, String humanReadible) {
        this.qdmMeasure = qdmMeasure;
        this.humanReadible = humanReadible;
    }
    
    public org.hl7.fhir.r4.model.Measure translateToFhir() {
        org.hl7.fhir.r4.model.Measure fhirMeasure = new org.hl7.fhir.r4.model.Measure();

            fhirMeasure.setId("Measure/"+qdmMeasure.getAbbrName());
            fhirMeasure.setName(qdmMeasure.getAbbrName());
            fhirMeasure.setDescription(qdmMeasure.getDescription());
            fhirMeasure.setDate(new Date());
            CodeableConcept concept1 = new CodeableConcept();
            Coding coding = new Coding();
            coding.setCode(qdmMeasure.getId());
            coding.setSystem("http://hl7.org/fhir/cqi/ecqm/Measure/Identifier/cms");
            Coding nqfCoding = new Coding();
            nqfCoding.setSystem("http://hl7.org/fhir/cqi/ecqm/Measure/Identifier/nqf");
            nqfCoding.setSystem(new String(qdmMeasure.getNqfNumber()));
            
            concept1.getCoding().add(coding);
            concept1.getCoding().add(nqfCoding);
            
            List<CodeableConcept> typeList = new ArrayList();
            CodeableConcept conceptType = new CodeableConcept();
            Coding codingType = new Coding();
            codingType.setCode("process");
            codingType.setSystem("http://hl7.org/fhir/measure-type");
            
            conceptType.addCoding(codingType);
            typeList.add(conceptType);
            fhirMeasure.setType(typeList);
            
            //need logic here
            String status = qdmMeasure.getMeasureStatus();
            if (status.equals("Draft")) fhirMeasure.setStatus(Enumerations.PublicationStatus.DRAFT);
            if (status.equals("Active"))fhirMeasure.setStatus(Enumerations.PublicationStatus.ACTIVE);
            if (status.equals("Retired")) fhirMeasure.setStatus(Enumerations.PublicationStatus.RETIRED);
            if (status.equals("Unknown")) fhirMeasure.setStatus(Enumerations.PublicationStatus.UNKNOWN);
            
            CodeableConcept conceptScore = new CodeableConcept();
            Coding codingScore = new Coding();
            codingScore.setCode(qdmMeasure.getScoring());
            codingScore.setSystem("http://hl7.org/fhir/measure-scoring");

            fhirMeasure.setScoring(conceptScore);
            
            //todo determine mapping to qdm
            fhirMeasure.setRationale("");
            
            org.hl7.fhir.r4.model.Period period = new org.hl7.fhir.r4.model.Period();
            period.setEnd(qdmMeasure.getMeasurementPeriodTo());
            period.setStart(qdmMeasure.getMeasurementPeriodFrom());
            fhirMeasure.setEffectivePeriod(period);
            
            org.hl7.fhir.r4.model.Narrative narrative = new org.hl7.fhir.r4.model.Narrative();
            narrative.setDivAsString(humanReadible);
            fhirMeasure.setText(narrative);
            
            //determine mapping
            fhirMeasure.setClinicalRecommendationStatement("");
            fhirMeasure.setGuidance("");
            fhirMeasure.setSupplementalData(new ArrayList());
            fhirMeasure.setGroup(new ArrayList());
           
//            ObjectMapper objMapper = new ObjectMapper();
//
//            try {
//                res = objMapper.writeValueAsString(fhirMeasure);
//            }
//            catch (Exception ex) {
//                LOGGER.log(Level.SEVERE, "Failed to convert to fhir "+qdmMeasure.getAbbrName()+" "+ex.getMessage());
//            }

        return fhirMeasure;
    }
}
