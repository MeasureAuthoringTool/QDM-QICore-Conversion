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

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;


@Slf4j
public class MeasureTranslator implements FhirCreator {
    //this should be something that MAT provides but doesn't there are many possibilities
    // TODO VALIDATION
    /*
      "severity" : "ERROR",
                "locationField" : "Measure",
                "errorDescription" : "Unable to locate profile http://hl7.org/fhir/us/qicore/StructureDefinition/qicore-task"
     */
    public static final String QI_CORE_MEASURE_PROFILE = "http://hl7.org/fhir/us/qicore/StructureDefinition/qicore-task";

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

        /*
         "severity" : "ERROR",
                "locationField" : "Measure.supplementalData[0]",
                "errorDescription" : "Profile http://hl7.org/fhir/StructureDefinition/Measure, Element 'Measure.supplementalData[0].criteria': minimum required = 1, but only found 0"
        */

        // For every patient evaluated by this measure also identify payer, race, ethnicity and sex


        //  fhirMeasure.setSupplementalData();
        // matCompositeMeasureModel.getSupplementalData();

        //set measure id
        fhirMeasure.setId(matCompositeMeasureModel.getId());
        fhirMeasure.setUrl(baseURL + "Measure/" + fhirMeasure.getId());
        fhirMeasure.setVersion(matCompositeMeasureModel.getVersionNumber());
        fhirMeasure.setName(matCompositeMeasureModel.getMeasureName());
        fhirMeasure.setTitle(matCompositeMeasureModel.getShortName());
        fhirMeasure.setPublisher(matCompositeMeasureModel.getStewardValue());
        fhirMeasure.setExperimental(false); //mat does not have experimental
        fhirMeasure.setDescription(matCompositeMeasureModel.getDescription());

        fhirMeasure.setUseContext(createUsageContext("purpose", "http://hl7.org/fhir/StructureDefinition/UsageContext", "displayname"));

        fhirMeasure.setPurpose("Unknown");
        fhirMeasure.setCopyright(matCompositeMeasureModel.getCopyright());
        fhirMeasure.setApprovalDate(convertDateTimeString(matCompositeMeasureModel.getFinalizedDate()));
        fhirMeasure.setDisclaimer(matCompositeMeasureModel.getDisclaimer());
        fhirMeasure.setRationale(matCompositeMeasureModel.getRationale());
        fhirMeasure.setClinicalRecommendationStatement(matCompositeMeasureModel.getClinicalRecomms());
        fhirMeasure.setGuidance(matCompositeMeasureModel.getGuidance());
        fhirMeasure.setContact(createContactDetailUrl("https://cms.gov")); //No  Contact Mapping

        processMeta(fhirMeasure);
        processHumanReadable(fhirMeasure);
        processIdentifiers(fhirMeasure);
        processExtensions(fhirMeasure);
        processStatus(fhirMeasure);
        processFinalizedDate(fhirMeasure);

        fhirMeasure.setJurisdiction(new ArrayList<>());
        fhirMeasure.getJurisdiction().add(buildCodeableConcept("US", "urn:iso:std:iso:3166", ""));

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


        //set scoring
        CodeableConcept scoringConcept = buildCodeableConcept(matCompositeMeasureModel.getMeasScoring(), "http://hl7.org/fhir/measure-scoring", "");
        fhirMeasure.setScoring(scoringConcept);
        processTypes(fhirMeasure);


        return fhirMeasure;
    }

    public void processFinalizedDate(Measure fhirMeasure) {
        if (matCompositeMeasureModel.getFinalizedDate() != null) {
            fhirMeasure.setApprovalDate(convertDateTimeString(matCompositeMeasureModel.getFinalizedDate()));
        } else {
            log.debug("No approval date");
        }
    }

    public void processStatus(Measure fhirMeasure) {
        //set measure status mat qdm does not have all status types
        if (matCompositeMeasureModel.isDraft()) {
            fhirMeasure.setStatus(Enumerations.PublicationStatus.DRAFT);
        } else if (matCompositeMeasureModel.isDeleted()) {
            fhirMeasure.setStatus(Enumerations.PublicationStatus.RETIRED);
        } else {
            fhirMeasure.setStatus(Enumerations.PublicationStatus.ACTIVE);
        }
    }

    public void processExtensions(Measure fhirMeasure) {
        //set Extensions if any known, QICore Extension below
        //QICore Not Done Extension
        //EncounterProcedureExtension
        //Military Service Extension
        //RAND Appropriateness Score Extension
        List<Extension> extensionList = new ArrayList<>();
        fhirMeasure.setExtension(extensionList);
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

    public void processMeta(Measure fhirMeasure) {
        Meta measureMeta = new Meta();
        // measureMeta.addProfile(QI_CORE_MEASURE_PROFILE);
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

    private List<UsageContext> createUsageContext(String code, String system, String display) {
        UsageContext usageContext = new UsageContext();
        Coding coding = new Coding();
        coding.setCode(code);
        coding.setSystem(system);
        coding.setDisplay(display);

        /* TODO validation
            {
                "severity" : "ERROR",
                "locationField" : "Measure.useContext[0]",
                "errorDescription" : "Profile http://hl7.org/fhir/StructureDefinition/UsageContext, Element 'Measure.useContext[0].value[x]': minimum required = 1, but only found 0"
            },
         */

        // usageContext.setValue()

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
}
