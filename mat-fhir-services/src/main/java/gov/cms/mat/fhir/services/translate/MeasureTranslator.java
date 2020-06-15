package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.commons.model.MeasureDetails;
import gov.cms.mat.fhir.commons.model.MeasureDetailsReference;
import gov.cms.mat.fhir.commons.model.MeasureReferenceType;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.repository.MeasureDetailsReferenceRepository;
import gov.cms.mat.fhir.services.repository.MeasureDetailsRepository;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import gov.cms.mat.fhir.services.repository.MeasureRepository;
import gov.cms.mat.fhir.services.translate.processor.MeasureGroupingDataProcessor;
import gov.cms.mat.fhir.services.translate.processor.RiskAdjustmentsDataProcessor;
import gov.cms.mat.fhir.services.translate.processor.SupplementalDataProcessor;
import lombok.extern.slf4j.Slf4j;
import mat.client.measure.ManageCompositeMeasureDetailModel;
import mat.client.measure.PeriodModel;
import mat.model.MeasureType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactDetail;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Identifier.IdentifierUse;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.UsageContext;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.INVALID_MEASURE_XML;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType.CITATION;
import static org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType.DOCUMENTATION;
import static org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType.JUSTIFICATION;


@Slf4j
@Service
public class MeasureTranslator extends TranslatorBase {
    //this should be something that MAT provides but doesn't there are many possibilities
    // TODO: close this issue.
    //public static final String QI_CORE_MEASURE_PROFILE = "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/proportion-measure-cqfm";
    //public static final String MEASURE_DATA_USAGE = "http://hl7.org/fhir/measure-data-usage";
    public static final RelatedArtifact.RelatedArtifactType DEFAULT_ARTIFACT_TYPE = DOCUMENTATION;
    public static final String MEASURE_TYPE = "http://hl7.org/fhir/measure-type";

    private final MeasureRepository matMeasureRepo;
    private final MeasureDetailsRepository matMeasureDetailsRepo;
    private final MeasureDetailsReferenceRepository matMeasureDetailsRefRepo;
    private final MeasureExportRepository matMeasureExportRepo;
    private final SupplementalDataProcessor supplementalDataProcessor;
    private final RiskAdjustmentsDataProcessor riskAdjustmentsDataProcessor;
    private final MeasureGroupingDataProcessor measureGroupingDataProcessor;
    private final CqlLibraryRepository cqlLibRepo;
    private final ManageMeasureDetailMapper measureDetailMapper;


    public MeasureTranslator(MeasureRepository matMeasureRepo,
                             MeasureDetailsRepository matMeasureDetailsRepo,
                             MeasureDetailsReferenceRepository matMeasureDetailsRefRepo,
                             MeasureExportRepository matMeasureExportRepo,
                             CqlLibraryRepository cqlLibRepo,
                             SupplementalDataProcessor supplementalDataProcessor,
                             RiskAdjustmentsDataProcessor riskAdjustmentsDataProcessor,
                             MeasureGroupingDataProcessor measureGroupingDataProcessor,
                             ManageMeasureDetailMapper measureDetailMapper) {
        this.matMeasureRepo = matMeasureRepo;
        this.matMeasureDetailsRepo = matMeasureDetailsRepo;
        this.matMeasureDetailsRefRepo = matMeasureDetailsRefRepo;
        this.matMeasureExportRepo = matMeasureExportRepo;
        this.cqlLibRepo = cqlLibRepo;
        this.supplementalDataProcessor = supplementalDataProcessor;
        this.riskAdjustmentsDataProcessor = riskAdjustmentsDataProcessor;
        this.measureGroupingDataProcessor = measureGroupingDataProcessor;
        this.measureDetailMapper = measureDetailMapper;
    }

    public ManageCompositeMeasureDetailModel buildModel(byte[] xmlBytes,
                                                        gov.cms.mat.fhir.commons.model.Measure matMeasure) {
        try {
            return measureDetailMapper.convert(xmlBytes, matMeasure);
        } catch (RuntimeException e) {
            ConversionReporter.setTerminalMessage(e.getMessage(), INVALID_MEASURE_XML);
            throw e;
        }
    }


    public Measure translateToFhir(String measureId) {
        var matMeasureOpt = matMeasureRepo.findById(measureId);
        var matExportOpt = matMeasureExportRepo.findByMeasureId(measureId);
        var cqlLib = cqlLibRepo.getCqlLibraryByMeasureId(measureId);
        if (matMeasureOpt.isEmpty()) {
            throw new RuntimeException("Can not find measure id " + measureId + " in the MAT DB.");
        }
        if (matExportOpt.isEmpty() || matExportOpt.get().getSimpleXml() == null) {
            throw new RuntimeException("Can not find simple xml for measure id " + measureId + " in the MAT DB.");
        }
        if (cqlLib == null) {
            throw new RuntimeException("Can not find measure lib for measure id " + measureId + " in the MAT DB.");
        }
        var matMeasure = matMeasureOpt.get();
        var simpleXml = matExportOpt.get().getSimpleXml();

        ManageCompositeMeasureDetailModel simpleXmlModel = buildModel(simpleXml, matMeasure);

        Measure result = new Measure();
        String id = matMeasure.getId();

        result.setId(id);
        result.setLanguage("en");
        result.setUrl(publicHapiFhirUrl + "Measure/" + id);
        result.setRationale(simpleXmlModel.getRationale());
        result.setClinicalRecommendationStatement(simpleXmlModel.getClinicalRecomms());
        result.setGuidance(simpleXmlModel.getGuidance());
        result.setVersion(createVersion(matMeasure));
        result.setName(simpleXmlModel.getMeasureName());
        result.setTitle(simpleXmlModel.getShortName());  //measure title
        result.setExperimental(false); //Mat does not have concept experimental
        result.setDescription(simpleXmlModel.getDescription());
        result.setPublisher(simpleXmlModel.getStewardValue());
        result.setPurpose("Unknown");
        result.setCopyright(simpleXmlModel.getCopyright());
        result.setDisclaimer(simpleXmlModel.getDisclaimer());
        result.setPurpose("Unknown");
        result.setLibrary(Collections.singletonList(new CanonicalType("Library/" + cqlLib.getId())));
        result.setTopic(createTopic());
        //set Extensions if any known, QICore Extension below
        //QICore Not Done Extension
        //EncounterProcedureExtension
        //Military Service Extension
        //RAND Appropriateness Score Extension
        result.setExtension(new ArrayList<>());
        result.setSubject(createType("http://hl7.org/fhir/resource-types","Patient"));
//        result.setJurisdiction();//TODO.
//        result.setCompositeScoring(); // TODO.
//        result.setImprovementNotation(); // TODO.


        result.setContact(createContactDetailUrl());

        result.setUseContext(createUsageContext());

        result.setEffectivePeriod(buildDefaultPeriod());

        // proessMeta(result);  TODO needs fixing
        processHumanReadable(id, result);
        processIdentifiers(result, simpleXmlModel);
        processStatus(result, simpleXmlModel);
        processFinalizeDate(result, simpleXmlModel);
        processTypes(result, simpleXmlModel);
        processJurisdiction(result);
        processPeriod(result, simpleXmlModel);
        processRelatedArtifacts(result, matMeasure, simpleXmlModel);
        processScoring(result, simpleXmlModel);
        processXml(simpleXml, result);

        return result;
    }

    private void processXml(byte[] xmlBytes, org.hl7.fhir.r4.model.Measure fhirMeasure) {
        String xml = new String(xmlBytes);

        fhirMeasure.setSupplementalData(supplementalDataProcessor.processXml(xml));

        fhirMeasure.setRiskAdjustment(riskAdjustmentsDataProcessor.processXml(xml));

        //Test for all types.
        fhirMeasure.setGroup(measureGroupingDataProcessor.processXml(xml));
    }


    public void processPeriod(Measure fhirMeasure,
                              ManageCompositeMeasureDetailModel matModel) {
        PeriodModel pModel = matModel.getPeriodModel();
        Period effectivePeriod = buildPeriod(convertDateTimeString(pModel.getStartDate()),
                convertDateTimeString(pModel.getStopDate()));
        fhirMeasure.setEffectivePeriod(effectivePeriod);
    }

    public void processJurisdiction(Measure fhirMeasure) {
        fhirMeasure.setJurisdiction(new ArrayList<>());
        fhirMeasure.getJurisdiction()
                .add(buildCodeableConcept("US", "urn:iso:std:iso:3166", ""));
    }

    public void processFinalizeDate(Measure fhirMeasure,
                                    ManageCompositeMeasureDetailModel matModel) {
        if (matModel.getFinalizedDate() != null) {
            fhirMeasure.setApprovalDate(convertDateTimeString(matModel.getFinalizedDate()));
        } else {
            log.debug("No approval date");
        }
    }

    public void processStatus(Measure fhirMeasure,
                              ManageCompositeMeasureDetailModel matModel) {
        //set measure status mat qdm does not have all status types
        if (matModel.isDraft()) {
            fhirMeasure.setStatus(Enumerations.PublicationStatus.DRAFT);
        } else if (matModel.isDeleted()) {
            fhirMeasure.setStatus(Enumerations.PublicationStatus.RETIRED);
        } else {
            fhirMeasure.setStatus(Enumerations.PublicationStatus.ACTIVE);
        }
    }

    public void processRelatedArtifacts(Measure fhirMeasure,
                                        gov.cms.mat.fhir.commons.model.Measure matMeasure,
                                        ManageCompositeMeasureDetailModel matModel) {
        List<RelatedArtifact> relatedArtifacts = new ArrayList<>();
        MeasureDetails details = matMeasureDetailsRepo.getMeasureDetailsByMeasureId(matMeasure.getId());
        Collection<MeasureDetailsReference> dbRefs =
                details == null ? null :
                        matMeasureDetailsRefRepo.getMeasureDetailsReferenceByMeasureDetailsId(details.getId());
        List<String> xmlRefs = matModel.getReferencesList();

        //Grab from DB if they are populated there, otherwise use the XML.
        if (CollectionUtils.isNotEmpty(dbRefs)) {
            dbRefs.forEach(r -> relatedArtifacts.add(convertReferenceDetails(r)));
        } else if (CollectionUtils.isNotEmpty(xmlRefs)) {
            xmlRefs.forEach(s -> relatedArtifacts.add(convertFromReference(s)));
        }

        fhirMeasure.setRelatedArtifact(relatedArtifacts);
    }

    public void processScoring(Measure fhirMeasure,
                               ManageCompositeMeasureDetailModel matModel) {
        //TO DO: Test scoring.
        CodeableConcept scoringConcept = buildCodeableConcept(matModel.getMeasScoring(),
                "http://hl7.org/fhir/measure-scoring", "");
        fhirMeasure.setScoring(scoringConcept);
    }

    public void processIdentifiers(Measure fhirMeasure, ManageCompositeMeasureDetailModel matModel) {
        fhirMeasure.setIdentifier(new ArrayList<>());

        if (matModel.geteMeasureId() != 0) {
            Identifier cms = createIdentifierOfficial("http://hl7.org/fhir/cqi/ecqm/Measure/Identifier/cms",
                    Integer.toString(matModel.geteMeasureId()));
            fhirMeasure.getIdentifier().add(cms);
        }


        if (BooleanUtils.isTrue(matModel.getEndorseByNQF())) {
            Identifier nqf = createIdentifierOfficial("http://hl7.org/fhir/cqi/ecqm/Measure/Identifier/nqf",
                    matModel.getNqfId());
            fhirMeasure.getIdentifier().add(nqf);
        }

    }

    public void processTypes(Measure fhirMeasure, ManageCompositeMeasureDetailModel matModel) {
        List<mat.model.MeasureType> matMeasureTypeTypeList = matModel.getMeasureTypeSelectedList();

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
        //TO DO: Test Type.
        var optional = MatMeasureType.findByMatAbbreviation(abbrName);

        if (optional.isPresent()) {
            return buildCodeableConcept(optional.get().fhirCode, MEASURE_TYPE, "");
        } else {
            return buildCodeableConcept("unknown", MEASURE_TYPE, "");
        }
    }

//    public void proecssMeta(Measure fhirMeasure, ManageCompositeMeasureDetailModel matModel) {
//        Meta measureMeta = new Meta();
//        measureMeta.addProfile(QI_CORE_MEASURE_PROFILE);
//        measureMeta.setVersionId(matModel.getVersionNumber());
//        measureMeta.setLastUpdated(new Date());
//        fhirMeasure.setMeta(measureMeta);
//    }

    public void processHumanReadable(String measureId, Measure measure) {
        var measureExpOpt = matMeasureExportRepo.findById(measureId);
        if (measureExpOpt.isPresent() && measureExpOpt.get().getHumanReadable() != null) {
            measure.setText(createNarrative(measureId, measureExpOpt.get().getHumanReadable()));
        }
    }


    private Identifier createIdentifierOfficial(String system, String code) {
        return new Identifier()
                .setSystem(system)
                .setUse(IdentifierUse.OFFICIAL)
                .setValue(code);
    }

    private List<ContactDetail> createContactDetailUrl() {
        ContactDetail contactDetail = new ContactDetail();
        contactDetail.setTelecom(new ArrayList<>());
        contactDetail.getTelecom().add(buildContactPoint());

        List<ContactDetail> contactDetails = new ArrayList<>(1);
        contactDetails.add(contactDetail);

        return contactDetails;
    }

    private ContactPoint buildContactPoint() {
        return new ContactPoint()
                .setValue("https://cms.gov")
                .setSystem(ContactPointSystem.URL);
    }

    private List<UsageContext> createUsageContext() {
        UsageContext usageContext = new UsageContext();
        Coding coding = new Coding();
        coding.setCode("program");
        usageContext.setCode(coding);

        CodeableConcept cc = new CodeableConcept();
        cc.setText("eligible-provider");
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

    private String createVersion(gov.cms.mat.fhir.commons.model.Measure matMeasure) {
        return createVersion(matMeasure.getVersion(), matMeasure.getRevisionNumber());
    }

    private Period buildDefaultPeriod() {
        LocalDate now = LocalDate.now();

        return new Period()
                .setStart(java.sql.Date.valueOf(now.with(firstDayOfYear())))
                .setEnd(java.sql.Date.valueOf(now.with(lastDayOfYear())));
    }

}
