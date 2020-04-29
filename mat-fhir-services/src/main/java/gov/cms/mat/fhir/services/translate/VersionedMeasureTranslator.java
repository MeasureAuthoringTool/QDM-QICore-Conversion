package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.commons.model.MeasureDetails;
import gov.cms.mat.fhir.commons.model.MeasureDetailsReference;
import gov.cms.mat.fhir.commons.model.MeasureReferenceType;
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

import static org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType.*;


@Slf4j
public class VersionedMeasureTranslator implements MeasureTranslator {
    //this should be something that MAT provides but doesn't there are many possibilities
    public static final String QI_CORE_MEASURE_PROFILE = "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/proportion-measure-cqfm";
    public static final String MEASURE_DATA_USAGE = "http://hl7.org/fhir/measure-data-usage";
    public static final RelatedArtifact.RelatedArtifactType DEFAULT_ARTIFACT_TYPE = DOCUMENTATION;

    public static final String MEASURE_TYPE = "http://hl7.org/fhir/measure-type";

    private final ManageCompositeMeasureDetailModel matCompositeMeasureModel;
    private final String humanReadable;

    private final String baseURL;
    private final gov.cms.mat.fhir.commons.model.Measure matMeasure;

    public VersionedMeasureTranslator(gov.cms.mat.fhir.commons.model.Measure matMeasure,
                                      ManageCompositeMeasureDetailModel measureCompositeModel,
                                      String humanReadable,
                                      String baseURL) {
        this.matCompositeMeasureModel = measureCompositeModel;
        this.humanReadable = humanReadable;
        this.baseURL = baseURL;
        this.matMeasure = matMeasure;
    }

    @Override
    public Measure translateToFhir(String uuid) {
        Measure fhirMeasure = buildMeasure();

        fhirMeasure.setId(uuid);
        fhirMeasure.setUrl(baseURL + "Measure/" + uuid);
        fhirMeasure.setRationale(matCompositeMeasureModel.getRationale());
        fhirMeasure.setClinicalRecommendationStatement(matCompositeMeasureModel.getClinicalRecomms());
        fhirMeasure.setGuidance(matCompositeMeasureModel.getGuidance());
        fhirMeasure.setVersion(matCompositeMeasureModel.getVersionNumber());
        fhirMeasure.setName(matCompositeMeasureModel.getMeasureName());
        fhirMeasure.setTitle(matCompositeMeasureModel.getShortName());  //measure title
        fhirMeasure.setExperimental(false); //Mat does not have concept experimental
        fhirMeasure.setDescription(matCompositeMeasureModel.getDescription());
        fhirMeasure.setPublisher(matCompositeMeasureModel.getStewardValue());
        fhirMeasure.setPurpose("Unknown");
        fhirMeasure.setCopyright(matCompositeMeasureModel.getCopyright());
        fhirMeasure.setDisclaimer(matCompositeMeasureModel.getDisclaimer());
        fhirMeasure.setPurpose("Unknown");

        //set Extensions if any known, QICore Extension below
        //QICore Not Done Extension
        //EncounterProcedureExtension
        //Military Service Extension
        //RAND Appropriateness Score Extension
        fhirMeasure.setExtension(new ArrayList<>());

        //TODO No  Contact Mapping
        fhirMeasure.setContact(createContactDetailUrl("https://cms.gov"));

        fhirMeasure.setUseContext(createUsageContext("program", "eligible-provider"));

        // proessMeta(fhirMeasure);  TODO needs fixing
        processHumanReadable(fhirMeasure);
        processIdentifiers(fhirMeasure);
        processStatus(fhirMeasure);
        processFinalizeDate(fhirMeasure);
        processTypes(fhirMeasure);
        processJurisdiction(fhirMeasure);
        processPeriod(fhirMeasure);
        processTopic(fhirMeasure);
        processRelatedArtifacts(fhirMeasure);
        processScoring(fhirMeasure);

        return fhirMeasure;
    }

    public void processTopic(Measure fhirMeasure) {
        fhirMeasure.setTopic(new ArrayList<>());
        fhirMeasure.getTopic().add(buildCodeableConcept("57024-2",
                "http://loinc.org",
                "Health Quality Measure Document"));
    }

    public void processPeriod(Measure fhirMeasure) {
        PeriodModel pModel = matCompositeMeasureModel.getPeriodModel();
        Period effectivePeriod = buildPeriod(convertDateTimeString(pModel.getStartDate()), convertDateTimeString(pModel.getStopDate()));
        fhirMeasure.setEffectivePeriod(effectivePeriod);
    }

    public void processJurisdiction(Measure fhirMeasure) {
        fhirMeasure.setJurisdiction(new ArrayList<>());
        fhirMeasure.getJurisdiction()
                .add(buildCodeableConcept("US", "urn:iso:std:iso:3166", ""));
    }

    public void processFinalizeDate(Measure fhirMeasure) {
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

    public void processRelatedArtifacts(Measure fhirMeasure) {
        List<RelatedArtifact> relatedArtifacts = new ArrayList<>();

        MeasureDetails matMeasureDetails = getMeasureDetails();
        Collection<MeasureDetailsReference> dbRefs =
                matMeasureDetails == null ? null : matMeasureDetails.getMeasureDetailsReferenceCollection();
        List<String> xmlRefs = matCompositeMeasureModel.getReferencesList();

        //Grab from DB if they are populated there, otherwise use the XML.
        if (CollectionUtils.isNotEmpty(dbRefs)) {
            dbRefs.forEach(r -> relatedArtifacts.add(convertReferenceDetails(r)));
        } else if (CollectionUtils.isNotEmpty(xmlRefs)) {
            xmlRefs.forEach(s -> relatedArtifacts.add(convertFromReference(s)));
        }

        fhirMeasure.setRelatedArtifact(relatedArtifacts);
    }

    public void processScoring(Measure fhirMeasure) {
        CodeableConcept scoringConcept = buildCodeableConcept(matCompositeMeasureModel.getMeasScoring(),
                "http://hl7.org/fhir/measure-scoring", "");
        fhirMeasure.setScoring(scoringConcept);
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
        List<mat.model.MeasureType> matMeasureTypeTypeList = matCompositeMeasureModel.getMeasureTypeSelectedList();

        if (CollectionUtils.isNotEmpty(matMeasureTypeTypeList)) {
            List<CodeableConcept> typeList = new ArrayList<>();
            for (MeasureType measureType : matMeasureTypeTypeList) {
                typeList.add(buildTypeFromAbbreviation(measureType.getAbbrName()));
            }
            fhirMeasure.setType(typeList);
        } else {
            log.info("No Mat Measure Types Found");
        }
    }

    public CodeableConcept buildTypeFromAbbreviation(String abbrName) {
        var optional = MatMeasureType.findByMatAbbreviation(abbrName);

        if (optional.isPresent()) {
            return buildCodeableConcept(optional.get().fhirCode, MEASURE_TYPE, "");
        } else {
            return buildCodeableConcept("unknown", MEASURE_TYPE, "");
        }
    }

    public void proecssMeta(Measure fhirMeasure) {
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

            } catch (Exception e) {
                log.info("Cannot process HumanReadable", e);
            }
        } else {
            log.debug("humanReadable is empty");
        }
    }

    private Identifier createIdentifierOfficial(String system, String code) {
        return new Identifier()
                .setSystem(system)
                .setUse(IdentifierUse.OFFICIAL)
                .setValue(code);
    }

    private List<ContactDetail> createContactDetailUrl(String url) {
        ContactDetail contactDetail = new ContactDetail();
        contactDetail.setTelecom(new ArrayList<>());
        contactDetail.getTelecom().add(buildContactPoint(url));

        List<ContactDetail> contactDetails = new ArrayList<>(1);
        contactDetails.add(contactDetail);

        return contactDetails;
    }

    private ContactPoint buildContactPoint(String url) {
        return new ContactPoint()
                .setValue(url)
                .setSystem(ContactPointSystem.URL);
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


    private Date convertDateTimeString(String dString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.parse(dString);
        } catch (Exception ex) {
            try {
                SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd/yyyy HH:mm a");
                return sdf2.parse(dString);
            } catch (Exception ex2) {
                LocalDate epoch = LocalDate.ofEpochDay(0L);
                long epochLong = epoch.toEpochDay();
                return new Date(epochLong);
            }
        }
    }

    private RelatedArtifact.RelatedArtifactType mapReferenceType(MeasureReferenceType matReferenceType) {
        switch (matReferenceType) {
            case JUSTIFICATION:
                return JUSTIFICATION;
            case CITATION:
                return CITATION;
            case DOCUMENTATION:
                return DOCUMENTATION;
            default:
                return DEFAULT_ARTIFACT_TYPE;
        }
    }

    private RelatedArtifact convertReferenceDetails(MeasureDetailsReference ref) {
        return new RelatedArtifact()
                .setCitation(ref.getReference())
                .setType(mapReferenceType(ref.getReferenceType()));
    }

    private RelatedArtifact convertFromReference(String ref) {
        return new RelatedArtifact()
                .setCitation(ref)
                .setType(DEFAULT_ARTIFACT_TYPE);
    }

    private MeasureDetails getMeasureDetails() {
        return CollectionUtils.isNotEmpty(
                matMeasure.getMeasureDetailsCollection()) ?
                matMeasure.getMeasureDetailsCollection().iterator().next() : null;
    }
}
