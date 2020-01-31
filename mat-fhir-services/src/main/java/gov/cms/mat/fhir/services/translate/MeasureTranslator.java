package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.services.translate.creators.FhirCreator;
import lombok.extern.slf4j.Slf4j;
import mat.client.measure.ManageCompositeMeasureDetailModel;
import mat.client.measure.PeriodModel;
import mat.model.MeasureType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.Identifier.IdentifierUse;
import org.hl7.fhir.r4.model.Measure.MeasureSupplementalDataComponent;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;


@Slf4j
public class MeasureTranslator implements FhirCreator {
    //this should be something that MAT provides but doesn't there are many possibilites
    public static final String QI_CORE_MEASURE_PROFILE = "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/proportion-measure-cqfm";
    public static final String MEASURE_DATA_USAGE = "http://hl7.org/fhir/measure-data-usage";

    public static final String MEASURE_TYPE = "http://hl7.org/fhir/measure-type";


    private final ManageCompositeMeasureDetailModel matCompositeMeasureModel;
    private final String humanReadable;

    private final String baseURL;


    public MeasureTranslator(ManageCompositeMeasureDetailModel measureCompositeModel, String humanReadable, String baseURL) {
        this.matCompositeMeasureModel = measureCompositeModel;
        this.humanReadable = humanReadable;
        this.baseURL = baseURL;
    }

    public org.hl7.fhir.r4.model.Measure translateToFhir() {
        org.hl7.fhir.r4.model.Measure fhirMeasure = new org.hl7.fhir.r4.model.Measure();

        //set measure id
        fhirMeasure.setId(matCompositeMeasureModel.getId());

        proessMeta(fhirMeasure);
        processHumanReadable(fhirMeasure);


        //set Extensions if any known, QICore Extension below
        //QICore Not Done Extension
        //EncounterProcedureExtension
        //Military Service Extension
        //RAND Appropriateness Score Extension
        List<Extension> extensionList = new ArrayList<>();
        fhirMeasure.setExtension(extensionList);

        //ConversionReporter.setMeasureResult("MAT.Unknown", "Measure.meta.extension", "No mapping available");


        //set the URL
        fhirMeasure.setUrl(baseURL + "Measure/" + fhirMeasure.getId());
        // ConversionReporter.setMeasureResult("MAT.Id", "Measure.url", "Generated From MAT Measure id (UUID)");
        processIdentifiers(fhirMeasure);


        fhirMeasure.setVersion(matCompositeMeasureModel.getVersionNumber());

        fhirMeasure.setName(matCompositeMeasureModel.getMeasureName());

        fhirMeasure.setTitle(matCompositeMeasureModel.getShortName());  //measure title

        //set measure status mat qdm does not have all status types
        if (matCompositeMeasureModel.isDraft()) {
            fhirMeasure.setStatus(Enumerations.PublicationStatus.DRAFT);
        } else if (matCompositeMeasureModel.isDeleted()) {
            fhirMeasure.setStatus(Enumerations.PublicationStatus.RETIRED);
        } else {
            fhirMeasure.setStatus(Enumerations.PublicationStatus.ACTIVE);
            // ConversionReporter.setMeasureResult("MAT.Unknown", "Measure.status", "Defaulting to ACTIVE neither draft or deleted");
        }

        //TODO measure experimental mat does not have concept

        boolean experimental = false;
        fhirMeasure.setExperimental(experimental);
        // ConversionReporter.setMeasureResult("MAT.Unknown", "Measure.experimental", "Default to false");

        if (matCompositeMeasureModel.getFinalizedDate() != null) {
            fhirMeasure.setApprovalDate(convertDateTimeString(matCompositeMeasureModel.getFinalizedDate()));
        } else {
            // ConversionReporter.setMeasureResult("MAT.finalizedDate", "Measure.approvalDate", "Finalized Date is NULL");
            log.debug("No approval date");
        }

        //set Publisher
        fhirMeasure.setPublisher(matCompositeMeasureModel.getStewardValue());


        //TODO No  Contact Mapping
        fhirMeasure.setContact(createContactDetailUrl("https://cms.gov"));
        //  ConversionReporter.setMeasureResult("MAT.Unknown", "Measure.contact", "No Mapping default to cms.gov");

        //Set Measure Description
        fhirMeasure.setDescription(matCompositeMeasureModel.getDescription());


        //set Use Context
        fhirMeasure.setUseContext(createUsageContext("program", "eligible-provider"));

        //juridiction
        List<CodeableConcept> jurisdictionList = new ArrayList<>();
        jurisdictionList.add(buildCodeableConcept("US", "urn:iso:std:iso:3166", ""));
        fhirMeasure.setJurisdiction(jurisdictionList);
        // ConversionReporter.setMeasureResult("MAT.Unknown", "Measure.jurisdiction", "No Mapping defaulting to US");

        //purpose
        fhirMeasure.setPurpose("Unknown");
        // ConversionReporter.setMeasureResult("MAT.Unknown", "Measure.purpose", "No Mapping defaulting to Unknown");


        //copyright
        fhirMeasure.setCopyright(matCompositeMeasureModel.getCopyright());

        //approval date
        fhirMeasure.setApprovalDate(convertDateTimeString(matCompositeMeasureModel.getFinalizedDate()));

        //TODO No concept of last reviewed date


        //set effective period
        PeriodModel pModel = matCompositeMeasureModel.getPeriodModel();
        Period effectivePeriod = buildPeriod(convertDateTimeString(pModel.getStartDate()), convertDateTimeString(pModel.getStopDate()));
        fhirMeasure.setEffectivePeriod(effectivePeriod);

        //topic
        List<CodeableConcept> topicList = new ArrayList<>();
        CodeableConcept topicCC = buildCodeableConcept("57024-2", "http://loinc.org", "Health Quality Measure Document");
        topicList.add(topicCC);
        fhirMeasure.setTopic(topicList);
        // ConversionReporter.setMeasureResult("MAT.Unknown", "Measure.topic", "No Mapping default Health Quality Measure Document");

        //related artifacts
        List<RelatedArtifact> relatedArtifacts = new ArrayList<>();
        List<String> referenceList = matCompositeMeasureModel.getReferencesList();
        if (referenceList != null) {
            for (String ref : referenceList) {
                RelatedArtifact art = new RelatedArtifact();
                art.setCitation(ref);
                art.setType(RelatedArtifact.RelatedArtifactType.CITATION);
                relatedArtifacts.add(art);
            }
            fhirMeasure.setRelatedArtifact(relatedArtifacts);
        }

        // if (relatedArtifacts.isEmpty()) {
        //    ConversionReporter.setMeasureResult("MAT.referencesList", "Measure.relatedArtifact", "NO Citations");
        // }


        //set disclaimer
        fhirMeasure.setDisclaimer(matCompositeMeasureModel.getDisclaimer());

        //set scoring
        CodeableConcept scoringConcept = buildCodeableConcept(matCompositeMeasureModel.getMeasScoring(), "http://hl7.org/fhir/measure-scoring", "");
        fhirMeasure.setScoring(scoringConcept);
        processTypes(fhirMeasure);

        //set rationale
        fhirMeasure.setRationale(matCompositeMeasureModel.getRationale());

        //set clinical recommendation
        fhirMeasure.setClinicalRecommendationStatement(matCompositeMeasureModel.getClinicalRecomms());

        //set guidance
        fhirMeasure.setGuidance(matCompositeMeasureModel.getGuidance());
        
        //set supplementalData
        //TODO mat return as string no processing logic
        //just add all for now
        fhirMeasure.setSupplementalData(processAllSupplementalData());

        
        


        return fhirMeasure;
    }

    public void processIdentifiers(Measure fhirMeasure) {
        fhirMeasure.setIdentifier(new ArrayList<>());

        if (matCompositeMeasureModel.geteMeasureId() != 0) {
            Identifier cms = createIdentifierOfficial("http://hl7.org/fhir/cqi/ecqm/Measure/Identifier/cms",
                    Integer.toString(matCompositeMeasureModel.geteMeasureId()));
            fhirMeasure.getIdentifier().add(cms);
        }


        if (BooleanUtils.isTrue(matCompositeMeasureModel.getEndorseByNQF())) {
            Identifier nqf = createIdentifierOfficial("http://hl7.org/fhir/cqi/ecqm/Measure/Identifier/nqf",
                    matCompositeMeasureModel.getNqfId());
            fhirMeasure.getIdentifier().add(nqf);
        }

    }

    public void processTypes(Measure fhirMeasure) {
        //Measure Type(s)
        List<mat.model.MeasureType> matMeasureTypeTypeList = matCompositeMeasureModel.getMeasureTypeSelectedList();

        if (CollectionUtils.isNotEmpty(matMeasureTypeTypeList)) {
            List<CodeableConcept> typeList = new ArrayList<>();
            for (MeasureType measureType : matMeasureTypeTypeList) {
                typeList.add(buildTypeFromAbbreviation(measureType.getAbbrName()));
            }
            fhirMeasure.setType(typeList);
        } else {
            log.info("No Mat Measure Types Found");
            // ConversionReporter.setMeasureResult("MAT.measureType", "Measure.type", "No Measure Types Found");
        }
    }

    public CodeableConcept buildTypeFromAbbreviation(String abbrName) {
        Optional<MatMeasureType> optional = MatMeasureType.findByMatAbbreviation(abbrName);

        if (optional.isPresent()) {
            return buildCodeableConcept(optional.get().fhirCode, MEASURE_TYPE, "");
        } else {
            return buildCodeableConcept("unknown", MEASURE_TYPE, "");
        }
    }

    public void proessMeta(Measure fhirMeasure) {
        //measure meta
        Meta measureMeta = new Meta();
        measureMeta.addProfile(QI_CORE_MEASURE_PROFILE);
        measureMeta.setVersionId(matCompositeMeasureModel.getVersionNumber());
        measureMeta.setLastUpdated(new Date());
        fhirMeasure.setMeta(measureMeta);
    }

    public void processHumanReadable(Measure fhirMeasure) {
        //set narrative
        if (!humanReadable.isEmpty()) {
            try {
                Narrative measureText = new Narrative();
                measureText.setStatusAsString("generated");
                //just encode it
                byte[] encodedText = Base64.getEncoder().encode(humanReadable.getBytes());
                measureText.setDivAsString(new String(encodedText));
                fhirMeasure.setText(measureText);
                //  ConversionReporter.setMeasureResult("MAT.humanReadible", "Measure.text", "Base64 Encoded Due to Format Issues");
            } catch (Exception ex) {
//                    Narrative measureText = new Narrative();
//                    measureText.setStatusAsString("generated");
//                    measureText.setDivAsString("[<![CDATA["+humanReadible+"]]>");
//                    fhirMeasure.setText(measureText);
            }
        } else {
            // ConversionReporter.setMeasureResult("MAT.humanReadible", "Measure.text", "Is Empty");
            log.debug("humanReadable is empty");
        }
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
        List<ContactPoint> lCP = new ArrayList<>();
        lCP.add(cP);
        contactDetail.setTelecom(lCP);

        List<ContactDetail> lCD = new ArrayList<>();
        lCD.add(contactDetail);

        return lCD;
    }

    private List<UsageContext> createUsageContext(String code, String value) {
        UsageContext usageContext = new UsageContext();
        Coding coding = new Coding();
        coding.setCode(code);
        usageContext.setCode(coding);
        
        CodeableConcept cc = new CodeableConcept();
        cc.setText(value);
        usageContext.setValue(cc);

        List<UsageContext> usageContextList = new ArrayList<>();
        usageContextList.add(usageContext);

        return usageContextList;
    }


    private Period buildPeriod(Date startDate, Date endDate) {
        Period p = new Period();
        p.setStart(startDate);
        p.setEnd(endDate);
        return p;
    }

    private Date convertDateTimeString(String dString) {
        // Date dt = new Date();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.parse(dString);
        } catch (Exception ex) {
            try {
                SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd/yyyy HH:mm a");
                //dString.replaceAll("AM", "A"); // TODO duane
                // dString.replaceAll("PM", "P");
                return sdf2.parse(dString);
            } catch (Exception ex2) {
                LocalDate epoch = LocalDate.ofEpochDay(0L);
                long epochLong = epoch.toEpochDay();
                return new Date(epochLong);
            }
        }

    }
    
    //TODO change this to process string returned MAT to determine whats required FOR fhir
    private List<MeasureSupplementalDataComponent> processAllSupplementalData() {
        List<MeasureSupplementalDataComponent> lSDE = new ArrayList<MeasureSupplementalDataComponent>();
        //process SDE ethinicity 
        MeasureSupplementalDataComponent sdeEth = new MeasureSupplementalDataComponent();
        sdeEth.setCode(processSupplementalDataCode("sde-ethnicity"));
        sdeEth.setUsage(processSupplementalDataUsage());
        sdeEth.setCriteria(processSupplementalDataCriteria("text/cql","SDE Ethnicity"));
        lSDE.add(sdeEth);
        //process SDE payer
        MeasureSupplementalDataComponent sdePayer = new MeasureSupplementalDataComponent();
        sdePayer.setCode(processSupplementalDataCode("sde-payer"));
        sdePayer.setUsage(processSupplementalDataUsage());
        sdePayer.setCriteria(processSupplementalDataCriteria("text/cql","SDE Payer"));
        lSDE.add(sdePayer);
        
        //process SDE Race
        MeasureSupplementalDataComponent sdeRace = new MeasureSupplementalDataComponent();
        sdeRace.setCode(processSupplementalDataCode("sde-race"));
        sdeRace.setUsage(processSupplementalDataUsage());
        sdeRace.setCriteria(processSupplementalDataCriteria("text/cql","SDE Race"));
        lSDE.add(sdeRace);
        
        //process SDE Sex
        MeasureSupplementalDataComponent sdeSex = new MeasureSupplementalDataComponent();
        sdeSex.setCode(processSupplementalDataCode("sde-sex"));
        sdeSex.setUsage(processSupplementalDataUsage());
        sdeSex.setCriteria(processSupplementalDataCriteria("text/cql","SDE Sex"));
        lSDE.add(sdeSex);       
        return lSDE;
    }
    
    private CodeableConcept processSupplementalDataCode(String code) {

        CodeableConcept sdeCode = new CodeableConcept();
        sdeCode.setText(code); 
        return sdeCode;
    }
    
    private List<CodeableConcept> processSupplementalDataUsage() {
        List<CodeableConcept> mList = new ArrayList<CodeableConcept>();
        CodeableConcept cc = new CodeableConcept();
        Coding c = new Coding();
        c.setSystem(MEASURE_DATA_USAGE);
        c.setCode("supplemental-data");
        cc.addCoding(c);
        mList.add(cc);
        return mList;
    }
    
    private Expression processSupplementalDataCriteria(String language, String expression) {
        Expression criteria = new Expression();
        criteria.setLanguage(language);
        criteria.setExpression(expression);
        
        return criteria;
    }
    
    
    

    
}
