package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.commons.model.MeasureDetails;
import gov.cms.mat.fhir.commons.model.MeasureDetailsReference;
import gov.cms.mat.fhir.commons.model.MeasureReferenceType;
import gov.cms.mat.fhir.services.components.reporting.ConversionReporter;
import gov.cms.mat.fhir.services.exceptions.CqlConversionException;
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
import mat.model.MeasureType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactDetail;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Identifier.IdentifierUse;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.UsageContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.INVALID_MEASURE_XML;
import static org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType.CITATION;
import static org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType.DOCUMENTATION;
import static org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType.JUSTIFICATION;


@Slf4j
@Service
public class MeasureTranslator extends TranslatorBase {
    public static final RelatedArtifact.RelatedArtifactType DEFAULT_ARTIFACT_TYPE = DOCUMENTATION;
    public static final String MEASURE_TYPE = "http://hl7.org/fhir/measure-type";

    public static final String EXTENSION_POPULATION_BASIS = "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-populationBasis";
    public static final String EXTENSION_SOFTWARE_SYSTEM = "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-softwaresystem";

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
        result.setExperimental(simpleXmlModel.isExperimental());
        result.setDescription(StringUtils.isBlank(simpleXmlModel.getDescription()) ? FHIR_UNKNOWN  :
                simpleXmlModel.getDescription());

        result.setPublisher(StringUtils.isBlank(simpleXmlModel.getStewardValue()) ? FHIR_UNKNOWN  : simpleXmlModel.getStewardValue());

        result.setPurpose(FHIR_UNKNOWN );
        result.setCopyright(simpleXmlModel.getCopyright());
        result.setDisclaimer(simpleXmlModel.getDisclaimer());
        result.setPurpose(FHIR_UNKNOWN );
        result.setLibrary(Collections.singletonList(new CanonicalType("Library/" + cqlLib.getId())));
        result.setContact(createContactDetailUrl());
        result.setUseContext(createUsageContext());
        result.setMeta(createMeasureMeta(simpleXmlModel.getMeasScoring()));
        processImprovementNotation(simpleXmlModel, result);
        processExtension(result);
        processContained(result);
        processHumanReadable(id, result);
        processIdentifiers(result, simpleXmlModel);
        processStatus(result, simpleXmlModel);
        processFinalizeDate(result, matMeasure);
        processTypes(result, simpleXmlModel);
        processJurisdiction(result);
        processPeriod(result, matMeasure);
        processRelatedArtifacts(result, matMeasure, simpleXmlModel);
        processScoring(result, simpleXmlModel);
        processXml(simpleXml, result);
        return result;

        //Note:
        // These are contextual and we might need to add them in later on.
        //result.setJurisdiction();
        //result.setCompositeScoring();
        //result.setSubject(createType("http://hl7.org/fhir/resource-types","Patient"));
        //result.setTopic(createTopic());
    }

    private Meta createMeasureMeta(String scoring) {
        Meta meta = new Meta(); //.addProfile("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/measure-cqfm");

        if (StringUtils.isBlank(scoring)) {
            log.error("Scoring type is null");
        } else {
            switch (scoring) {
                case "Proportion":
                    meta.addProfile("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/proportion-measure-cqfm");
                    break;
                case "Cohort":
                    meta.addProfile("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cohort-measure-cqfm");
                    break;
                case "Continuous Variable":
                    meta.addProfile("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cv-measure-cqfm");
                    break;
                case "Ratio":
                    meta.addProfile("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/ratio-measure-cqfm");
                    break;
                default:
                    log.error("Cannot find scoring type for scoring: {}", scoring);
            }
        }
        return meta;
    }

    private void processImprovementNotation(ManageCompositeMeasureDetailModel simpleXmlModel, Measure fhirMeasure) {
        if (StringUtils.isBlank(simpleXmlModel.getImprovNotations())) {
            throw new CqlConversionException("simpleMeasureXml.getImprovementNotations() can not be blank.");
        }
        if (!StringUtils.equals(simpleXmlModel.getImprovNotations(), "increase") &&
                !StringUtils.equals(simpleXmlModel.getImprovNotations(), "decrease")) {
            throw new CqlConversionException("invalid simpleMeasureXml.getImprovementNotations(), " +
                    simpleXmlModel.getImprovNotations() +
                    " must be either increase or decrease.");
        }

        fhirMeasure.setImprovementNotation(buildCodeableConcept(simpleXmlModel.getImprovNotations(),
                "http://terminology.hl7.org/CodeSystem/measure-improvement-notation",
                null));
    }

    private void processExtension(Measure fhirMeasure) {
        fhirMeasure.setExtension(new ArrayList<>());
        fhirMeasure.getExtension().add(new Extension(EXTENSION_POPULATION_BASIS, new CodeType("boolean")));
        fhirMeasure.getExtension().add(new Extension(EXTENSION_SOFTWARE_SYSTEM, new Reference("#cqf-tooling")));
    }

    private void processContained(Measure fhirMeasure) {
        Device device = new Device();
        Meta meta = new Meta();
        meta.setProfile(Collections.singletonList(new CanonicalType("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/device-softwaresystem-cqfm")));
        device.setMeta(meta);

        device.setId("cqf-tooling");

        //todo carson/stan what to place here
        Device.DeviceDeviceNameComponent nameComponent = new Device.DeviceDeviceNameComponent();
        nameComponent.setName("cqf-tooling");
        nameComponent.setType(Device.DeviceNameType.MANUFACTURERNAME);
        device.addDeviceName(nameComponent);

        Device.DeviceVersionComponent deviceVersionComponent = new Device.DeviceVersionComponent();
        deviceVersionComponent.setValue("1.1.0-SNAPSHOT");
        device.setVersion(Collections.singletonList(deviceVersionComponent));

        CodeableConcept codeableConcept = new CodeableConcept();
        Coding coding = new Coding();
        coding.setSystem("http://hl7.org/fhir/us/cqfmeasures/CodeSystem/software-system-type");
        coding.setCode("tooling");
        codeableConcept.setCoding(Collections.singletonList(coding));

        device.setType(codeableConcept);

        fhirMeasure.setContained(Collections.singletonList(device));
    }

    private void processXml(byte[] xmlBytes, org.hl7.fhir.r4.model.Measure fhirMeasure) {
        String xml = new String(xmlBytes);
        fhirMeasure.setSupplementalData(supplementalDataProcessor.processXml(xml));
        fhirMeasure.setRiskAdjustment(riskAdjustmentsDataProcessor.processXml(xml));
        fhirMeasure.setGroup(measureGroupingDataProcessor.processXml(xml));
    }

    public void processPeriod(Measure fhirMeasure,
                              gov.cms.mat.fhir.commons.model.Measure matModel) {
        Period effectivePeriod = buildPeriodDayResolution(matModel.getMeasurementPeriodFrom(), matModel.getMeasurementPeriodTo());
        fhirMeasure.setEffectivePeriod(effectivePeriod);
    }

    public void processJurisdiction(Measure fhirMeasure) {
        fhirMeasure.setJurisdiction(new ArrayList<>());
        fhirMeasure.getJurisdiction()
                .add(buildCodeableConcept("US", "urn:iso:std:iso:3166", ""));
    }

    public void processFinalizeDate(Measure fhirMeasure,
                                    gov.cms.mat.fhir.commons.model.Measure matModel) {
        if (matModel.getFinalizedDate() != null) {
            fhirMeasure.setApprovalDate(matModel.getFinalizedDate());
        } else {
            log.debug("No approval date");
        }
    }

    public void processStatus(Measure fhirMeasure,
                              ManageCompositeMeasureDetailModel matModel) {
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

        String code = matModel.getMeasScoring().toLowerCase();

        if (code.equals("continuous variable")) {
            code = "continuous-variable";
        }

        String system = "http://terminology.hl7.org/CodeSystem/measure-scoring";
        String display = matModel.getMeasScoring();

        CodeableConcept scoringConcept = buildCodeableConcept(code, system, display);
        fhirMeasure.setScoring(scoringConcept);
    }

    public void processIdentifiers(Measure fhirMeasure, ManageCompositeMeasureDetailModel matModel) {
        fhirMeasure.setIdentifier(new ArrayList<>());
        if(matModel.getMeasureSetId() != null) {
            fhirMeasure.getIdentifier()
                    .add(createIdentifierOfficial("http://hl7.org/fhir/cqi/ecqm/Measure/Identifier/guid",
                            matModel.getMeasureSetId()));
        }

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
            fhirMeasure.setType(List.of(buildCodeableConcept(FHIR_UNKNOWN, MEASURE_TYPE, "")));
            log.info("No Mat Measure Types Found set default to {}", FHIR_UNKNOWN);
        }
    }

    public CodeableConcept buildTypeFromAbbreviation(String abbrName) {
        var optional = MatMeasureType.findByMatAbbreviation(abbrName);

        if (optional.isPresent()) {
            return buildCodeableConcept(optional.get().fhirCode, MEASURE_TYPE, "");
        } else {
            return buildCodeableConcept(FHIR_UNKNOWN, MEASURE_TYPE, "");
        }
    }

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
}
