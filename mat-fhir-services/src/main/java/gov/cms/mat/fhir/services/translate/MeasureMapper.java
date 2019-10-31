/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.commons.model.MeasureDetailsReference;
import gov.cms.mat.fhir.services.translate.creators.FhirCreator;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.Identifier.IdentifierUse;
import org.hl7.fhir.r4.model.Measure.MeasureGroupComponent;
import org.hl7.fhir.r4.model.Measure.MeasureSupplementalDataComponent;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
/**
 *
 * @author duanedecouteau
 */
public class MeasureMapper implements FhirCreator {
    public static final Logger LOGGER = Logger.getLogger(MeasureMapper.class.getName());
    //this should be something that MAT provides but doesn't there are many possibilites
    public static final String QI_CORE_MEASURE_PROFILE = "http://hl7.org/fhir/us/qicore/StructureDefinition/qicore-task";
    
    private gov.cms.mat.fhir.commons.model.Measure qdmMeasure;
    private gov.cms.mat.fhir.commons.model.MeasureDetails qdmMeasureDetails;
    private List<gov.cms.mat.fhir.commons.model.MeasureDetailsReference> qdmCitations = new ArrayList();
    private String humanReadible;
    
    @Value("${fhir.r4.baseurl}")
    private String baseURL = "http://localhost:8080/hapi-fhir-jpaserver/fhir/";
    
    public MeasureMapper(gov.cms.mat.fhir.commons.model.Measure qdmMeasure, gov.cms.mat.fhir.commons.model.MeasureDetails qdmMeasureDetails, List<MeasureDetailsReference> qdmCitations) {
        this.qdmMeasure = qdmMeasure;
        this.qdmMeasureDetails = qdmMeasureDetails;
        this.qdmCitations = qdmCitations;
    }
    
    public org.hl7.fhir.r4.model.Measure translateToFhir() {
        org.hl7.fhir.r4.model.Measure fhirMeasure = new org.hl7.fhir.r4.model.Measure();
        
            //set measure id
            fhirMeasure.setId(qdmMeasure.getId());
            //measure meta
            Meta measureMeta = new Meta();
            measureMeta.addProfile(QI_CORE_MEASURE_PROFILE);
            measureMeta.setVersionId(qdmMeasure.getVersion().toString());
            measureMeta.setLastUpdated(qdmMeasure.getLastModifiedOn());
            
            fhirMeasure.setMeta(measureMeta);
            
            //set narrative
            Narrative measureText = new Narrative();
            measureText.setStatusAsString("generated");
            measureText.setDivAsString("");  //qdm human readible
            
            fhirMeasure.setText(measureText);
            
            //set Extensions if any known, QICore Extension below
            //QICore Not Done Extension
            //EncounterProcedureExtension
            //Military Service Extension
            //RAND Appropriateness Score Extension            
            List<Extension> extensionList = new ArrayList();
            fhirMeasure.setExtension(extensionList);
            
            
            //set the URL
            fhirMeasure.setUrl(baseURL+"Measure/"+fhirMeasure.getId());
            
            //set identifiers cms and nqf if available
            List<Identifier> idList = new ArrayList();
            Identifier cms = null;
            Identifier nqf = null;
            if (qdmMeasure.getEmeasureId() != null) {
                cms = createIdentifierOfficial("http://hl7.org/fhir/cqi/ecqm/Measure/Identifier/cms", qdmMeasure.getEmeasureId().toString());
            }
            if (qdmMeasure.getNqfNumber() != null) {
                nqf = createIdentifierOfficial("http://hl7.org/fhir/cqi/ecqm/Measure/Identifier/nqf", new String(qdmMeasure.getNqfNumber()));
            }
            if (cms != null) idList.add(cms);
            if (nqf != null) idList.add(nqf);
            
            fhirMeasure.setIdentifier(idList);
            
            fhirMeasure.setVersion(qdmMeasure.getVersion().toString());
            
            fhirMeasure.setName(qdmMeasure.getAbbrName());
            
            fhirMeasure.setTitle(qdmMeasure.getAbbrName());  //measure title
            
            //set measure status mat qdm does not have all status types
            String status = qdmMeasure.getMeasureStatus();
            if (status != null) {
                if (status.equals("In Progress")) fhirMeasure.setStatus(Enumerations.PublicationStatus.DRAFT);
                if (status.equals("Complete"))fhirMeasure.setStatus(Enumerations.PublicationStatus.ACTIVE);
            }
            //if measure experimental mat does not have concept
            boolean experimental = false;
            fhirMeasure.setExperimental(experimental);
            
            fhirMeasure.setApprovalDate(qdmMeasure.getFinalizedDate());
            
            //set Publisher
            fhirMeasure.setPublisher("Centers for Medicare & Medicaid Services");
            
            
            //set 
            fhirMeasure.setContact(createContactDetailUrl("https://cms.gov"));
       
            
            fhirMeasure.setDescription(qdmMeasure.getDescription());
            
            //set Use Context
            fhirMeasure.setUseContext(createUsageContext("purpose", "codesystem", "displayname"));
            
            //juridiction
            List<CodeableConcept> jurisdictionList = new ArrayList();
            jurisdictionList.add(buildCodeableConcept("US", "urn:iso:std:iso:3166", ""));
            
            //purpose
            fhirMeasure.setPurpose(qdmMeasure.getDescription());
            
            //copyright
            fhirMeasure.setCopyright(qdmMeasureDetails.getCopyright());
            
            //approval date
            fhirMeasure.setApprovalDate(qdmMeasure.getFinalizedDate());
            
            //last reviewed date
            fhirMeasure.setLastReviewDate(qdmMeasure.getLockedOutDate());
            
            //set effective period
            Period effectivePeriod = buildPeriod(qdmMeasure.getMeasurementPeriodFrom(), qdmMeasure.getMeasurementPeriodTo());
            fhirMeasure.setEffectivePeriod(effectivePeriod);
            
            //topic
            List<CodeableConcept> topicList = new ArrayList();
            CodeableConcept topicCC = buildCodeableConcept("57024-2", "http://loinc.org", "Health Quality Measure Document");
            topicList.add(topicCC);
            fhirMeasure.setTopic(topicList);
            
            //related artifacts
            List<RelatedArtifact> relatedArtifacts = new ArrayList();
            Iterator iter = qdmCitations.iterator();
            while (iter.hasNext()) {
                MeasureDetailsReference ref = (MeasureDetailsReference)iter.next();
                RelatedArtifact art = new RelatedArtifact();
                art.setCitation(ref.getReference());
                art.setType(RelatedArtifact.RelatedArtifactType.CITATION);
                relatedArtifacts.add(art);
            }
            fhirMeasure.setRelatedArtifact(relatedArtifacts);
            
            
            //set disclaimer
            fhirMeasure.setDisclaimer(qdmMeasureDetails.getDisclaimer());
            
            //set scoring
            CodeableConcept scoringConcept = buildCodeableConcept(qdmMeasure.getScoring(), "http://hl7.org/fhir/measure-scoring", "");
            fhirMeasure.setScoring(scoringConcept);
            
            //set type no MAT reference
            List<CodeableConcept> typeList = new ArrayList();
            CodeableConcept typeConcept = buildCodeableConcept("process", "http://hl7.org/fhir/measure-type", "");
            typeList.add(typeConcept);
            fhirMeasure.setType(typeList);
            
            //set rationale
            fhirMeasure.setRationale(qdmMeasureDetails.getRationale());
            
            //set clinical recommendation
            fhirMeasure.setClinicalRecommendationStatement(qdmMeasureDetails.getClinicalRecommendation());
            
            //set guidance
            fhirMeasure.setGuidance(qdmMeasureDetails.getGuidance());
            
            //set group
            List<MeasureGroupComponent> listMGC = new ArrayList();
            if (qdmMeasureDetails.getInitialPopulation() != null) {
                MeasureGroupComponent initialPopulation = new MeasureGroupComponent();
                initialPopulation.setCode(buildCodeableConcept("initial-population", "http://terminology.hl7.org/CodeSystem/measure-population", "Initial Population"));
                initialPopulation.setDescription(qdmMeasureDetails.getInitialPopulation());
                listMGC.add(initialPopulation);
            }
            
            if (qdmMeasureDetails.getDenominator() != null) {
                MeasureGroupComponent denominator = new MeasureGroupComponent();
                denominator.setCode(buildCodeableConcept("denominator", "http://terminology.hl7.org/CodeSystem/measure-population", "Denominator"));
                denominator.setDescription(qdmMeasureDetails.getDenominator());
                listMGC.add(denominator);
            }
            
            if (qdmMeasureDetails.getDenominatorExclusions() != null) {
                MeasureGroupComponent denominatorExclusions = new MeasureGroupComponent();
                denominatorExclusions.setCode(buildCodeableConcept("denominator-exclusions", "http://terminology.hl7.org/CodeSystem/measure-population", "Denominator Exclusions"));
                denominatorExclusions.setDescription(qdmMeasureDetails.getDenominatorExclusions());
                listMGC.add(denominatorExclusions);
            }
            
            if (qdmMeasureDetails.getDenominatorExceptions() != null) {
                MeasureGroupComponent denominatorExceptions = new MeasureGroupComponent();
                denominatorExceptions.setCode(buildCodeableConcept("denominator-exceptions", "http://terminology.hl7.org/CodeSystem/measure-population", "Denominator Exceptions"));
                denominatorExceptions.setDescription(qdmMeasureDetails.getDenominatorExceptions());
                listMGC.add(denominatorExceptions);
            }
            
            if (qdmMeasureDetails.getNumerator() != null) {
                MeasureGroupComponent numerator = new MeasureGroupComponent();
                numerator.setCode(buildCodeableConcept("numerator", "http://terminology.hl7.org/CodeSystem/measure-population", "Numerator"));
                numerator.setDescription(qdmMeasureDetails.getNumerator());
                listMGC.add(numerator);
            }
            
            if (qdmMeasureDetails.getNumeratorExclusions() != null) {
                MeasureGroupComponent numeratorExclusions = new MeasureGroupComponent();
                numeratorExclusions.setCode(buildCodeableConcept("numerator-exclusions", "http://terminology.hl7.org/CodeSystem/measure-population", "Numerator Exclusions"));
                numeratorExclusions.setDescription(qdmMeasureDetails.getNumerator());
                listMGC.add(numeratorExclusions);
            }
            
            fhirMeasure.setGroup(listMGC);
            
            //supplemental data
            List<MeasureSupplementalDataComponent> mSDC = new ArrayList();
            if (qdmMeasureDetails.getSupplementalDataElements() != null) {
                MeasureSupplementalDataComponent sComp =  new MeasureSupplementalDataComponent();
                sComp.setCode(buildCodeableConcept("supplemental-data", "http://hl7.org/fhir/measure-data-usage", ""));
                sComp.setDescription(qdmMeasureDetails.getSupplementalDataElements());
                mSDC.add(sComp);
            }
            fhirMeasure.setSupplementalData(mSDC);

        return fhirMeasure;
    }
    
    private Identifier createIdentifierOfficial(String system, String code) {
        Identifier id = new Identifier();
        id.setSystem(system);
        IdentifierUse useId = IdentifierUse.OFFICIAL;
        id.setUse(useId);
        id.setValue(code);
        
        return id;
    }
    
    private List<ContactDetail> createContactDetailUrl(String url) {
        ContactDetail contactDetail = new ContactDetail();
        ContactPoint cP = new ContactPoint();
        ContactPointSystem cPS = ContactPointSystem.URL;
        cP.setValue(url);
        cP.setSystem(cPS);
        List<ContactPoint> lCP = new ArrayList();
        lCP.add(cP);
        contactDetail.setTelecom(lCP);
       
        List<ContactDetail> lCD = new ArrayList();
        lCD.add(contactDetail);
        
        return lCD;
    }
    
    private List<UsageContext> createUsageContext(String code, String system, String display) {
        UsageContext uC = new UsageContext();
        Coding coding = new Coding();
        coding.setCode(code);
        coding.setSystem(system);
        coding.setDisplay(display);
        uC.setCode(coding);
        
        List<UsageContext> lUC = new ArrayList();
        lUC.add(uC);
        
        return lUC;
    }
    
    private CodeableConcept buildCodeableConcept(String code, String system, String display) {
        CodeableConcept cp = new CodeableConcept();
        List<Coding> lC = new ArrayList();
        Coding cd = new Coding();
        cd.setCode(code);
        cd.setSystem(system);
        cd.setDisplay(display);
        lC.add(cd);
        cp.setCoding(lC);
        
        return cp;
    }
    
    private Period buildPeriod(Date startdate, Date enddate) {
        Period p = new Period();
        p.setStart(startdate);
        p.setEnd(enddate);
        
        return p;
    }
}
