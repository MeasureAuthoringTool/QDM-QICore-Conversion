package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.commons.model.MeasureDetailsReference;
import gov.cms.mat.fhir.commons.model.MeasureReferenceType;
import gov.cms.mat.fhir.commons.model.MeasureTypeAssociation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;

import java.time.LocalDate;
import java.util.*;

import static org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType.*;

@Slf4j
public class DraftMeasureTranslator implements MeasureTranslator {
    //this should be something that MAT provides but doesn't there are many possibilities
    public static final String QI_CORE_MEASURE_PROFILE = "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/proportion-measure-cqfm";
    public static final String MEASURE_DATA_USAGE = "http://hl7.org/fhir/measure-data-usage";
    public static final RelatedArtifact.RelatedArtifactType DEFAULT_ARTIFACT_TYPE = DOCUMENTATION;

    public static final String MEASURE_TYPE = "http://hl7.org/fhir/measure-type";

    private final String humanReadable;

    private final String baseURL;
    private final gov.cms.mat.fhir.commons.model.Measure matMeasure;
    private final gov.cms.mat.fhir.commons.model.MeasureDetails matDetails;

    public DraftMeasureTranslator(gov.cms.mat.fhir.commons.model.Measure matMeasure,
                                  String humanReadable,
                                  String baseURL) {
        this.humanReadable = humanReadable;
        this.baseURL = baseURL;
        this.matMeasure = matMeasure;
        this.matDetails = matMeasure.getMeasureDetailsCollection().iterator().next();
    }

    @Override
    public Measure translateToFhir(String uuid) {
        Measure fhirMeasure = buildMeasure();

        fhirMeasure.setId(matMeasure.getId());
        fhirMeasure.setUrl(baseURL + "Measure/" + fhirMeasure.getId());
        fhirMeasure.setRationale(matDetails.getRationale());
        fhirMeasure.setClinicalRecommendationStatement(matDetails.getClinicalRecommendation());
        fhirMeasure.setGuidance(matDetails.getGuidance());
        fhirMeasure.setVersion(createVersion(matMeasure));

        fhirMeasure.setName(matMeasure.getCqlName());
        fhirMeasure.setTitle(matMeasure.getAbbrName());  //measure title
        fhirMeasure.setExperimental(false); //Mat does not have concept experimental
        fhirMeasure.setDescription(matDetails.getDescription());
        fhirMeasure.setPublisher(matMeasure.getMeasureStewardId() == null ? "" : matMeasure.getMeasureStewardId().getOrgName());
        fhirMeasure.setPurpose("Unknown");
        fhirMeasure.setCopyright(matDetails.getCopyright());
        fhirMeasure.setDisclaimer(matDetails.getDisclaimer());


        //set Extensions if any known, QICore Extension below
        //QICore Not Done Extension
        //EncounterProcedureExtension
        //Military Service Extension
        //RAND Appropriateness Score Extension
        fhirMeasure.setExtension(new ArrayList<>());

        //TODO No  Contact Mapping
        fhirMeasure.setContact(createContactDetailUrl("https://cms.gov"));

        fhirMeasure.setUseContext(createUsageContext("program", "eligible-provider"));

        //TO DO: Needs fixing.
        //proessMeta(fhirMeasure);
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
        Date to = matMeasure.getMeasurementPeriodTo();
        Date from = matMeasure.getMeasurementPeriodFrom();
        Period effectivePeriod = null;
        if (to != null && from != null) {
            effectivePeriod = buildPeriod(from, to);
        } else {
            LocalDate epoch = LocalDate.ofEpochDay(0L);
            long epochLong = epoch.toEpochDay();
            Date d = new Date(epochLong);
            effectivePeriod = buildPeriod(d, d);
        }
        fhirMeasure.setEffectivePeriod(effectivePeriod);
    }

    public void processJurisdiction(Measure fhirMeasure) {
        fhirMeasure.setJurisdiction(new ArrayList<>());
        fhirMeasure.getJurisdiction()
                .add(buildCodeableConcept("US", "urn:iso:std:iso:3166", ""));
    }

    public void processFinalizeDate(Measure fhirMeasure) {
        fhirMeasure.setApprovalDate(new Date());
    }

    public void processStatus(Measure fhirMeasure) {
        //set measure status mat qdm does not have all status types
        if (matMeasure.getDraft()) {
            fhirMeasure.setStatus(Enumerations.PublicationStatus.DRAFT);
        } else if (StringUtils.isNotEmpty(matMeasure.getDeleted())) {
            fhirMeasure.setStatus(Enumerations.PublicationStatus.RETIRED);
        } else {
            fhirMeasure.setStatus(Enumerations.PublicationStatus.ACTIVE);
        }
    }

    public void processRelatedArtifacts(Measure fhirMeasure) {
        List<RelatedArtifact> relatedArtifacts = new ArrayList<>();
        Collection<MeasureDetailsReference> dbRefs = matDetails.getMeasureDetailsReferenceCollection();

        if (CollectionUtils.isNotEmpty(dbRefs)) {
            dbRefs.forEach(r -> relatedArtifacts.add(convertReferenceDetails(r)));
        }
        fhirMeasure.setRelatedArtifact(relatedArtifacts);
    }

    public void processScoring(Measure fhirMeasure) {
        CodeableConcept scoringConcept = buildCodeableConcept(matMeasure.getScoring(),
                "http://hl7.org/fhir/measure-scoring", "");
        fhirMeasure.setScoring(scoringConcept);
    }

    public void processIdentifiers(Measure fhirMeasure) {
        fhirMeasure.setIdentifier(new ArrayList<>());

        if (matMeasure.getEmeasureId() != 0) {
            Identifier cms = createIdentifierOfficial("http://hl7.org/fhir/cqi/ecqm/Measure/Identifier/cms",
                    Integer.toString(matMeasure.getEmeasureId()));
            fhirMeasure.getIdentifier().add(cms);
        }
    }

    public void processTypes(Measure fhirMeasure) {
        Collection<MeasureTypeAssociation> matTypeList = matMeasure.getMeasureTypeAssociationCollection();

        if (CollectionUtils.isNotEmpty(matTypeList)) {
            List<CodeableConcept> typeList = new ArrayList<>();
            matTypeList.forEach(mta ->
                    typeList.add(buildTypeFromAbbreviation(mta.getMeasureTypeId().getAbbrName())));
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
                .setUse(Identifier.IdentifierUse.OFFICIAL)
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
                .setSystem(ContactPoint.ContactPointSystem.URL);
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

    private String getVersion() {
        return matMeasure.getVersion() + "." + matMeasure.getVersion();
    }
}

