/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.services.translate.creators.FhirCreator;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumerations;
import java.time.LocalDate;
import mat.client.measure.ManageCompositeMeasureDetailModel;
import mat.client.measure.PeriodModel;
import org.hl7.fhir.r4.model.ContactDetail;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.Identifier.IdentifierUse;
import org.hl7.fhir.r4.model.Measure.MeasureGroupComponent;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import java.util.Base64;
import org.hl7.fhir.r4.model.Measure.MeasureSupplementalDataComponent;

/**
 *
 * @author duanedecouteau
 */
public class MeasureMapper implements FhirCreator {
    public static final Logger LOGGER = Logger.getLogger(MeasureMapper.class.getName());
    //this should be something that MAT provides but doesn't there are many possibilites
    public static final String QI_CORE_MEASURE_PROFILE = "http://hl7.org/fhir/us/qicore/StructureDefinition/qicore-task";
    
//    private gov.cms.mat.fhir.commons.model.Measure qdmMeasure;
//    private gov.cms.mat.fhir.commons.model.MeasureDetails qdmMeasureDetails;
//    private List<gov.cms.mat.fhir.commons.model.MeasureDetailsReference> qdmCitations = new ArrayList();
//    private MeasureExport qdmMeasureExport;
    private ManageCompositeMeasureDetailModel mModel;
    private String humanReadible;
    
    private String baseURL;
    
//    public MeasureMapper(gov.cms.mat.fhir.commons.model.Measure qdmMeasure, gov.cms.mat.fhir.commons.model.MeasureDetails qdmMeasureDetails, List<MeasureDetailsReference> qdmCitations, MeasureExport qdmMeasureExport) {
//        this.qdmMeasure = qdmMeasure;
//        this.qdmMeasureDetails = qdmMeasureDetails;
//        this.qdmCitations = qdmCitations;
//        this.qdmMeasureExport = qdmMeasureExport;
//    }
    
    public MeasureMapper(ManageCompositeMeasureDetailModel measureCompositeModel, String humanReadible, String baseURL) {
        this.mModel = measureCompositeModel;
        this.humanReadible = humanReadible;
        this.baseURL = baseURL;
    }
    
    public org.hl7.fhir.r4.model.Measure translateToFhir() {
        org.hl7.fhir.r4.model.Measure fhirMeasure = new org.hl7.fhir.r4.model.Measure();
        
            //set measure id
            fhirMeasure.setId(mModel.getId());
            //measure meta
            Meta measureMeta = new Meta();
            measureMeta.addProfile(QI_CORE_MEASURE_PROFILE);
            measureMeta.setVersionId(mModel.getVersionNumber());
            measureMeta.setLastUpdated(new Date());
            
            fhirMeasure.setMeta(measureMeta);
            
            //set narrative
            if (!humanReadible.isEmpty()) {
                try {
                    Narrative measureText = new Narrative();
                    measureText.setStatusAsString("generated");
                    //just encode it 
                    byte[] encodedText = Base64.getEncoder().encode(humanReadible.getBytes());
                    measureText.setDivAsString(new String(encodedText));
                    fhirMeasure.setText(measureText);
                    ConversionReporter.setMeasureResult("MAT.humanReadible", "Measure.narrative", "Base64 Encoded Due to Format Issues");
                }
                catch (Exception ex) {
//                    Narrative measureText = new Narrative();
//                    measureText.setStatusAsString("generated");
//                    measureText.setDivAsString("[<![CDATA["+humanReadible+"]]>");
//                    fhirMeasure.setText(measureText);
                }
            }
            else {
                ConversionReporter.setMeasureResult("MAT.humanReadible", "Measure.narrative", "Is Empty");
            }
            
            //set Extensions if any known, QICore Extension below
            //QICore Not Done Extension
            //EncounterProcedureExtension
            //Military Service Extension
            //RAND Appropriateness Score Extension            
            List<Extension> extensionList = new ArrayList<>();
            fhirMeasure.setExtension(extensionList);
            ConversionReporter.setMeasureResult("MAT.Unknown", "Measure.extension", "No mapping available");
            
            
            //set the URL
            fhirMeasure.setUrl(baseURL+"Measure/"+fhirMeasure.getId());
            ConversionReporter.setMeasureResult("MAT.Id", "Measure.url", "Generated From MAT Measure id (UUID)");
            
            //set identifiers cms and nqf if available
            List<Identifier> idList = new ArrayList<>();
            Identifier cms = null;
            Identifier nqf = null;
            if (mModel.geteMeasureId() != 0) {
                cms = createIdentifierOfficial("http://hl7.org/fhir/cqi/ecqm/Measure/Identifier/cms", new Integer(mModel.geteMeasureId()).toString());
                idList.add(cms);
            }
            if (mModel.getEndorseByNQF()) {
                nqf = createIdentifierOfficial("http://hl7.org/fhir/cqi/ecqm/Measure/Identifier/nqf", new String(mModel.getNqfId()));
                idList.add(nqf);
            }            
            fhirMeasure.setIdentifier(idList);
            if (idList.isEmpty()) {
                ConversionReporter.setMeasureResult("MAT.eMeasureId", "Measure.identifier", "Not Available");
                ConversionReporter.setMeasureResult("MAT.nqfId", "Measure.identifier", "Not Available");
            }
            
            fhirMeasure.setVersion(mModel.getVersionNumber());
            
            fhirMeasure.setName(mModel.getMeasureName());
            
            fhirMeasure.setTitle(mModel.getShortName());  //measure title
            
            //set measure status mat qdm does not have all status types
            if (mModel.isDraft()) {
                fhirMeasure.setStatus(Enumerations.PublicationStatus.DRAFT);
            } 
            else if(mModel.isDeleted()) {    
                fhirMeasure.setStatus(Enumerations.PublicationStatus.RETIRED);
            }
            else {
                fhirMeasure.setStatus(Enumerations.PublicationStatus.ACTIVE);
                ConversionReporter.setMeasureResult("MAT.isDraft", "Measure.status", "Defaulting to ACTIVE neither draft or deleted");
            }
            
            //TODO measure experimental mat does not have concept

            boolean experimental = false;
            fhirMeasure.setExperimental(experimental);
            ConversionReporter.setMeasureResult("MAT.Unknown", "Measure.experimental", "Default to false");
            
            if (mModel.getFinalizedDate() != null) {
                fhirMeasure.setApprovalDate(convertDateTimeString(mModel.getFinalizedDate()));
            } else {
              ConversionReporter.setMeasureResult("MAT.FinalizedDate", "Measure.approvalDate", "Finalized Date is NULL");
            }
            
            //set Publisher
            fhirMeasure.setPublisher(mModel.getStewardValue());

            
            //TODO No  Contact Mapping 
            fhirMeasure.setContact(createContactDetailUrl("https://cms.gov"));
            ConversionReporter.setMeasureResult("MAT.Unknown", "Measure.contact", "No Mapping default to cms.gov");
       
            //Set Measure Description
            fhirMeasure.setDescription(mModel.getDescription());
            
            
            //set Use Context
            fhirMeasure.setUseContext(createUsageContext("purpose", "codesystem", "displayname"));
            
            //juridiction
            List<CodeableConcept> jurisdictionList = new ArrayList<>();
            jurisdictionList.add(buildCodeableConcept("US", "urn:iso:std:iso:3166", ""));
            fhirMeasure.setJurisdiction(jurisdictionList);
            ConversionReporter.setMeasureResult("MAT.Unknown", "Measure.Jurisdiction", "No Mapping defaulting to US");
            
            //purpose
            fhirMeasure.setPurpose(mModel.getDescription());
            
            //copyright
            fhirMeasure.setCopyright(mModel.getCopyright());
            
            //approval date
            fhirMeasure.setApprovalDate(convertDateTimeString(mModel.getFinalizedDate()));
            
            //TODO No concept of last reviewed date
            
            
            //set effective period
            PeriodModel pModel = mModel.getPeriodModel();
            Period effectivePeriod = buildPeriod(convertDateTimeString(pModel.getStartDate()), convertDateTimeString(pModel.getStopDate()));
            fhirMeasure.setEffectivePeriod(effectivePeriod);
            
            //topic
            List<CodeableConcept> topicList = new ArrayList<>();
            CodeableConcept topicCC = buildCodeableConcept("57024-2", "http://loinc.org", "Health Quality Measure Document");
            topicList.add(topicCC);
            fhirMeasure.setTopic(topicList);
            ConversionReporter.setMeasureResult("MAT.Unknown", "Measure.Topic", "No Mapping default Health Quality Measure Document");
            
            //related artifacts
            List<RelatedArtifact> relatedArtifacts = new ArrayList<>();
            List<String> referenceList = mModel.getReferencesList();
            Iterator iter = referenceList.iterator();
            while (iter.hasNext()) {
                String ref = (String)iter.next();
                RelatedArtifact art = new RelatedArtifact();
                art.setCitation(ref);
                art.setType(RelatedArtifact.RelatedArtifactType.CITATION);
                relatedArtifacts.add(art);
            }
            fhirMeasure.setRelatedArtifact(relatedArtifacts);
            if (relatedArtifacts.isEmpty()) {
                ConversionReporter.setMeasureResult("MAT.referenceList", "Measure.relatedArtifacts", "NO Citations");
            }
            
            
            //set disclaimer
            fhirMeasure.setDisclaimer(mModel.getDisclaimer());
            
            //set scoring
            CodeableConcept scoringConcept = buildCodeableConcept(mModel.getCompositeScoringMethod(), "http://hl7.org/fhir/measure-scoring", "");
            fhirMeasure.setScoring(scoringConcept);
            
            //Measure Type(s)
            List<CodeableConcept> typeList = new ArrayList<>();
            List<mat.model.MeasureType> matTypeList = mModel.getMeasureTypeSelectedList();
            Iterator mIter = matTypeList.iterator();
            while (mIter.hasNext()) {
                mat.model.MeasureType mType = (mat.model.MeasureType)mIter.next();
                String abbrName = mType.getAbbrName();
                if (abbrName.equals("COMPOSITE")) {
                    CodeableConcept compositeConcept = buildCodeableConcept("composite", "http://hl7.org/fhir/measure-type", "");
                    typeList.add(compositeConcept);
                }
                else if(abbrName.equals("INTERM-OM") || abbrName.equals("OUTCOME")) {
                    CodeableConcept outcomeConcept = buildCodeableConcept("outcome", "http://hl7.org/fhir/measure-type", "");
                    typeList.add(outcomeConcept);
                }
                else if(abbrName.equals("PRO-PM")) {
                    CodeableConcept patientConcept = buildCodeableConcept("patient-report-outcome", "http://hl7.org/fhir/measure-type", "");
                    typeList.add(patientConcept);                    
                }
                else if(abbrName.equals("STRUCTURE") || abbrName.equals("RESOURCE")) {
                    CodeableConcept structureConcept = buildCodeableConcept("structure", "http://hl7.org/fhir/measure-type", "");
                    typeList.add(structureConcept);                                        
                }
                else if(abbrName.equals("APPROPRIATE") || abbrName.equals("EFFICIENCY") || abbrName.equals("PROCESS")) {
                    CodeableConcept processConcept = buildCodeableConcept("process", "http://hl7.org/fhir/measure-type", "");
                    typeList.add(processConcept);                                                            
                }
                else {
                    CodeableConcept unk = buildCodeableConcept("unknown", "http://hl7.org/fhir/measure-type", "");
                    typeList.add(unk); 
                    ConversionReporter.setMeasureResult("MeasureType.abbrName", "Measure.type", "Default to unknown not matching Abbr name");
                }
            }
            fhirMeasure.setType(typeList);
            
            //set rationale
            fhirMeasure.setRationale(mModel.getRationale());
            
            //set clinical recommendation
            fhirMeasure.setClinicalRecommendationStatement(mModel.getClinicalRecomms());
            
            //set guidance
            fhirMeasure.setGuidance(mModel.getGuidance());
            
            //set group
            List<MeasureGroupComponent> listMGC = new ArrayList<>();
            if (mModel.getInitialPop() != null) {
                MeasureGroupComponent initialPopulation = new MeasureGroupComponent();
                initialPopulation.setCode(buildCodeableConcept("initial-population", "http://terminology.hl7.org/CodeSystem/measure-population", "Initial Population"));
                initialPopulation.setDescription(mModel.getInitialPop());
                listMGC.add(initialPopulation);
            }
            
            if (mModel.getDenominator() != null) {
                MeasureGroupComponent denominator = new MeasureGroupComponent();
                denominator.setCode(buildCodeableConcept("denominator", "http://terminology.hl7.org/CodeSystem/measure-population", "Denominator"));
                denominator.setDescription(mModel.getDenominator());
                listMGC.add(denominator);
            }
            
            if (mModel.getDenominatorExclusions() != null) {
                MeasureGroupComponent denominatorExclusions = new MeasureGroupComponent();
                denominatorExclusions.setCode(buildCodeableConcept("denominator-exclusions", "http://terminology.hl7.org/CodeSystem/measure-population", "Denominator Exclusions"));
                denominatorExclusions.setDescription(mModel.getDenominatorExclusions());
                listMGC.add(denominatorExclusions);
            }
            
            if (mModel.getDenominatorExceptions() != null) {
                MeasureGroupComponent denominatorExceptions = new MeasureGroupComponent();
                denominatorExceptions.setCode(buildCodeableConcept("denominator-exceptions", "http://terminology.hl7.org/CodeSystem/measure-population", "Denominator Exceptions"));
                denominatorExceptions.setDescription(mModel.getDenominatorExceptions());
                listMGC.add(denominatorExceptions);
            }
            
            if (mModel.getNumerator() != null) {
                MeasureGroupComponent numerator = new MeasureGroupComponent();
                numerator.setCode(buildCodeableConcept("numerator", "http://terminology.hl7.org/CodeSystem/measure-population", "Numerator"));
                numerator.setDescription(mModel.getNumerator());
                listMGC.add(numerator);
            }
            
            if (mModel.getNumeratorExclusions() != null) {
                MeasureGroupComponent numeratorExclusions = new MeasureGroupComponent();
                numeratorExclusions.setCode(buildCodeableConcept("numerator-exclusions", "http://terminology.hl7.org/CodeSystem/measure-population", "Numerator Exclusions"));
                numeratorExclusions.setDescription(mModel.getNumerator());
                listMGC.add(numeratorExclusions);
            }
            
            fhirMeasure.setGroup(listMGC);
            if (listMGC.isEmpty()) {
                ConversionReporter.setMeasureResult("MAT.manyfields", "Measure.group", "FAIL There are no MeasureGroupComponents for this measure");
            }
            
            
            //TODO manage composite missing detail supplemental data
            List<MeasureSupplementalDataComponent> mSDC = new ArrayList<>();
            if (mModel.getSupplementalData() != null) {
                MeasureSupplementalDataComponent sComp =  new MeasureSupplementalDataComponent();
                sComp.setCode(buildCodeableConcept("supplemental-data", "http://hl7.org/fhir/measure-data-usage", ""));
                sComp.setDescription(mModel.getSupplementalData());
                mSDC.add(sComp);
            }
            fhirMeasure.setSupplementalData(mSDC);
            if (mSDC.isEmpty()) {
                ConversionReporter.setMeasureResult("MAT.supplementalData", "Measure.supplementalData", "No SupplementalData");
            }

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
        
        List<UsageContext> lUC = new ArrayList<>();
        lUC.add(uC);
        
        return lUC;
    }
    
    private CodeableConcept buildCodeableConcept(String code, String system, String display) {
        CodeableConcept cp = new CodeableConcept();
        List<Coding> lC = new ArrayList<>();
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
    
    private Date convertDateTimeString(String dString) {
        Date dt = new Date();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dt = sdf.parse(dString);
        }
        catch (Exception ex) {
            try {
                SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd/yyyy HH:mm a");
                dString.replaceAll("AM", "A");
                dString.replaceAll("PM", "P");
                dt = sdf2.parse(dString);
            } catch (Exception ex2) {
                LocalDate epoch = LocalDate.ofEpochDay( 0L );
                long epochLong = epoch.toEpochDay();
                dt = new Date(epochLong);
            }
        }
        return dt;
    }
}
