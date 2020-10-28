package gov.cms.mat.patients.conversion.service;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.AdverseEventConverter;
import gov.cms.mat.patients.conversion.conversion.AllergyIntoleranceConverter;
import gov.cms.mat.patients.conversion.conversion.AssessmentOrderConverter;
import gov.cms.mat.patients.conversion.conversion.AssessmentPerformedConverter;
import gov.cms.mat.patients.conversion.conversion.AssessmentRecommendedConverter;
import gov.cms.mat.patients.conversion.conversion.CareCoalConverter;
import gov.cms.mat.patients.conversion.conversion.CommunicationPerformedConverter;
import gov.cms.mat.patients.conversion.conversion.ConverterBase;
import gov.cms.mat.patients.conversion.conversion.DeviceAppliedConverter;
import gov.cms.mat.patients.conversion.conversion.DiagnosisConverter;
import gov.cms.mat.patients.conversion.conversion.DiagnosticStudyOrderConverter;
import gov.cms.mat.patients.conversion.conversion.DiagnosticStudyPerformedConverter;
import gov.cms.mat.patients.conversion.conversion.EncounterConverter;
import gov.cms.mat.patients.conversion.conversion.EncounterOrderConverter;
import gov.cms.mat.patients.conversion.conversion.FamilyHistoryConverter;
import gov.cms.mat.patients.conversion.conversion.ImmunizationAdministeredConverter;
import gov.cms.mat.patients.conversion.conversion.ImmunizationOrderConverter;
import gov.cms.mat.patients.conversion.conversion.InterventionOrderConverter;
import gov.cms.mat.patients.conversion.conversion.InterventionPerformedConverter;
import gov.cms.mat.patients.conversion.conversion.InterventionRecommendedConverter;
import gov.cms.mat.patients.conversion.conversion.LaboratoryTestOrderConverter;
import gov.cms.mat.patients.conversion.conversion.LaboratoryTestPerformedConverter;
import gov.cms.mat.patients.conversion.conversion.MedicationActiveConverter;
import gov.cms.mat.patients.conversion.conversion.MedicationAdministeredConverter;
import gov.cms.mat.patients.conversion.conversion.MedicationDischargeConverter;
import gov.cms.mat.patients.conversion.conversion.MedicationDispensedConverter;
import gov.cms.mat.patients.conversion.conversion.ParticipationConverter;
import gov.cms.mat.patients.conversion.conversion.PatientConverter;
import gov.cms.mat.patients.conversion.conversion.PhysicalExamPerformedConverter;
import gov.cms.mat.patients.conversion.conversion.ProcedureOrderConverter;
import gov.cms.mat.patients.conversion.conversion.ProcedurePerformedConverter;
import gov.cms.mat.patients.conversion.conversion.helpers.FhirCreator;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirPatientResult;
import gov.cms.mat.patients.conversion.dao.BonniePatient;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.data.ConversionResult;
import gov.cms.mat.patients.conversion.data.ConvertedPatient;
import gov.cms.mat.patients.conversion.data.FhirDataElement;
import gov.cms.mat.patients.conversion.exceptions.PatientConversionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PatientService implements FhirCreator {
    private static final int THREAD_POOL_TIMEOUT_MINUTES = 1;
    private final PatientConverter patientConverter;
    private final EncounterConverter encounterConverter;
    private final InterventionOrderConverter interventionOrderConverter;
    private final InterventionPerformedConverter interventionPerformedConverter;
    private final MedicationDischargeConverter medicationDischargeConverter;
    private final AdverseEventConverter adverseEventConverter;
    private final AllergyIntoleranceConverter allergyIntoleranceConverter;
    private final AssessmentOrderConverter assessmentOrderConverter;
    private final AssessmentPerformedConverter assessmentPerformedConverter;
    private final AssessmentRecommendedConverter assessmentRecommendedConverter;
    private final CareCoalConverter careCoalConverter;
    private final CommunicationPerformedConverter communicationPerformedConverter;
    private final DiagnosisConverter diagnosisConverter;
    private final DiagnosticStudyOrderConverter diagnosticStudyOrderConverter;
    private final DeviceAppliedConverter deviceAppliedConverter;
    private final DiagnosticStudyPerformedConverter diagnosticStudyPerformedConverter;
    private final EncounterOrderConverter encounterOrderConverter;
    private final FamilyHistoryConverter familyHistoryConverter;
    private final InterventionRecommendedConverter interventionRecommendedConverter;
    private final LaboratoryTestOrderConverter laboratoryTestOrderConverter;
    private final LaboratoryTestPerformedConverter laboratoryTestPerformedConverter;
    private final MedicationActiveConverter medicationActiveConverter;
    private final MedicationAdministeredConverter medicationAdministeredConverter;
    private final MedicationDispensedConverter medicationDispensedConverter;
    private final ImmunizationAdministeredConverter immunizationAdministeredConverter;
    private final ImmunizationOrderConverter immunizationOrderConverter;
    private final ParticipationConverter participationConverter;
    private final PhysicalExamPerformedConverter physicalExamPerformedConverter;
    private final ProcedureOrderConverter procedureOrderConverter;
    private final ProcedurePerformedConverter procedurePerformedConverter;

    private final ObjectMapper objectMapper;
    private final FhirContext fhirContext;

    public PatientService(PatientConverter patientConverter,
                          EncounterConverter encounterConverter,
                          InterventionOrderConverter interventionOrderConverter,
                          InterventionPerformedConverter interventionPerformedConverter,
                          MedicationDischargeConverter medicationDischargeConverter,
                          AdverseEventConverter adverseEventConverter,
                          AllergyIntoleranceConverter allergyIntoleranceConverter,
                          AssessmentOrderConverter assessmentOrderConverter,
                          AssessmentPerformedConverter assessmentPerformedConverter,
                          AssessmentRecommendedConverter assessmentRecommendedConverter,
                          CareCoalConverter careCoalConverter,
                          CommunicationPerformedConverter communicationPerformedConverter,
                          DiagnosisConverter diagnosisConverter,
                          DiagnosticStudyOrderConverter diagnosticStudyOrderConverter,
                          DiagnosticStudyPerformedConverter diagnosticStudyPerformedConverter,
                          FamilyHistoryConverter familyHistoryConverter,
                          InterventionRecommendedConverter interventionRecommendedConverter,
                          LaboratoryTestOrderConverter laboratoryTestOrderConverter,
                          ImmunizationOrderConverter immunizationOrderConverter,
                          DeviceAppliedConverter deviceAppliedConverter,
                          EncounterOrderConverter encounterOrderConverter,
                          LaboratoryTestPerformedConverter laboratoryTestPerformedConverter,
                          MedicationActiveConverter medicationActiveConverter,
                          MedicationAdministeredConverter medicationAdministeredConverter,
                          MedicationDispensedConverter medicationDispensedConverter,
                          ImmunizationAdministeredConverter immunizationAdministeredConverter,
                          ParticipationConverter participationConverter,
                          PhysicalExamPerformedConverter physicalExamPerformedConverter,
                          ProcedureOrderConverter procedureOrderConverter,
                          ProcedurePerformedConverter procedurePerformedConverter, ObjectMapper objectMapper,
                          FhirContext fhirContext) {
        this.patientConverter = patientConverter;
        this.encounterConverter = encounterConverter;
        this.interventionOrderConverter = interventionOrderConverter;
        this.interventionPerformedConverter = interventionPerformedConverter;
        this.medicationDischargeConverter = medicationDischargeConverter;
        this.adverseEventConverter = adverseEventConverter;
        this.allergyIntoleranceConverter = allergyIntoleranceConverter;
        this.assessmentOrderConverter = assessmentOrderConverter;
        this.assessmentPerformedConverter = assessmentPerformedConverter;
        this.assessmentRecommendedConverter = assessmentRecommendedConverter;
        this.careCoalConverter = careCoalConverter;
        this.communicationPerformedConverter = communicationPerformedConverter;
        this.diagnosisConverter = diagnosisConverter;
        this.diagnosticStudyOrderConverter = diagnosticStudyOrderConverter;
        this.diagnosticStudyPerformedConverter = diagnosticStudyPerformedConverter;
        this.familyHistoryConverter = familyHistoryConverter;
        this.interventionRecommendedConverter = interventionRecommendedConverter;
        this.laboratoryTestOrderConverter = laboratoryTestOrderConverter;
        this.immunizationOrderConverter = immunizationOrderConverter;
        this.deviceAppliedConverter = deviceAppliedConverter;
        this.medicationActiveConverter = medicationActiveConverter;
        this.medicationAdministeredConverter = medicationAdministeredConverter;
        this.procedurePerformedConverter = procedurePerformedConverter;
        this.fhirContext = fhirContext;
        this.encounterOrderConverter = encounterOrderConverter;
        this.laboratoryTestPerformedConverter = laboratoryTestPerformedConverter;
        this.immunizationAdministeredConverter = immunizationAdministeredConverter;
        this.medicationDispensedConverter = medicationDispensedConverter;
        this.participationConverter = participationConverter;
        this.physicalExamPerformedConverter = physicalExamPerformedConverter;
        this.procedureOrderConverter = procedureOrderConverter;
        this.objectMapper = objectMapper;
    }

    public List<ConversionResult> processMany(List<BonniePatient> bonniePatients) {
        List<ConversionResult> results = bonniePatients.parallelStream()
                .map(this::processOne)
                .collect(Collectors.toList());

        //  maintain the order from the input list
        return bonniePatients.stream()
                .map(b -> findResultById(b.get_id(), results))
                .collect(Collectors.toList());
    }

    public ConversionResult processOne(BonniePatient bonniePatient) {
        try {
            List<CompletableFuture<List<FhirDataElement>>> futures = new ArrayList<>();

            var qdmTypes = collectQdmTypes(bonniePatient);

            QdmToFhirPatientResult qdmToFhirPatientResult = patientConverter.convert(bonniePatient);
            Patient fhirPatient = qdmToFhirPatientResult.getFhirPatient();

            if (qdmTypes.contains(EncounterConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, encounterConverter, futures);
            }

            if (qdmTypes.contains(InterventionOrderConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, interventionOrderConverter, futures);
            }

            if (qdmTypes.contains(InterventionPerformedConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, interventionPerformedConverter, futures);
            }

            if (qdmTypes.contains(MedicationDischargeConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, medicationDischargeConverter, futures);
            }

            if (qdmTypes.contains(AdverseEventConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, adverseEventConverter, futures);
            }

            if (qdmTypes.contains(AllergyIntoleranceConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, allergyIntoleranceConverter, futures);
            }

            if (qdmTypes.contains(AssessmentOrderConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, assessmentOrderConverter, futures);
            }

            if (qdmTypes.contains(AssessmentPerformedConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, assessmentPerformedConverter, futures);
            }

            if (qdmTypes.contains(AssessmentRecommendedConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, assessmentRecommendedConverter, futures);
            }

            if (qdmTypes.contains(CareCoalConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, careCoalConverter, futures);
            }

            if (qdmTypes.contains(CommunicationPerformedConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, communicationPerformedConverter, futures);
            }

            if (qdmTypes.contains(DiagnosisConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, diagnosisConverter, futures);
            }

            if (qdmTypes.contains(DiagnosticStudyOrderConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, diagnosticStudyOrderConverter, futures);
            }

            if (qdmTypes.contains(DiagnosticStudyPerformedConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, diagnosticStudyPerformedConverter, futures);
            }

            if (qdmTypes.contains(DeviceAppliedConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, deviceAppliedConverter, futures);
            }

            if (qdmTypes.contains(EncounterOrderConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, encounterOrderConverter, futures);
            }

            if (qdmTypes.contains(FamilyHistoryConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, familyHistoryConverter, futures);
            }

            if (qdmTypes.contains(InterventionRecommendedConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, interventionRecommendedConverter, futures);
            }

            if (qdmTypes.contains(LaboratoryTestOrderConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, laboratoryTestOrderConverter, futures);
            }

            if (qdmTypes.contains(LaboratoryTestPerformedConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, laboratoryTestPerformedConverter, futures);
            }

            if (qdmTypes.contains(ImmunizationAdministeredConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, immunizationAdministeredConverter, futures);
            }

            if (qdmTypes.contains(ImmunizationOrderConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, immunizationOrderConverter, futures);
            }

            if (qdmTypes.contains(MedicationActiveConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, medicationActiveConverter, futures);
            }

            if (qdmTypes.contains(MedicationAdministeredConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, medicationAdministeredConverter, futures);
            }

            if (qdmTypes.contains(MedicationDispensedConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, medicationDispensedConverter, futures);
            }

            if (qdmTypes.contains(ParticipationConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, participationConverter, futures);
            }

            if (qdmTypes.contains(PhysicalExamPerformedConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, physicalExamPerformedConverter, futures);
            }

            if (qdmTypes.contains(ProcedureOrderConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, procedureOrderConverter, futures);
            }

            if (qdmTypes.contains(ProcedurePerformedConverter.QDM_TYPE)) {
                processFuture(bonniePatient, fhirPatient, procedurePerformedConverter, futures);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

            List<FhirDataElement> fhirDataElements = findFhirDataElementsFromFutures(futures);

            ConvertedPatient convertedPatient = ConvertedPatient.builder()
                    .fhirPatient(objectMapper.readTree(toJson(fhirContext, fhirPatient)))
                    .outcome(qdmToFhirPatientResult.getOutcome())
                    .build();

            Instant timeStamp = Instant.now();
            return ConversionResult.builder()
                    .id(bonniePatient.get_id())
                    .expectedValues(bonniePatient.getExpectedValues())
                    .measureIds(bonniePatient.getMeasureIds())
                    .convertedPatient(convertedPatient)
                    .dataElements(fhirDataElements)
                    .createdAt(timeStamp)
                    .updatedAt(timeStamp)
                    .build();

        } catch (Exception e) {
            log.warn("Error ", e);
            throw new PatientConversionException("Error", e);
        }
    }

    private List<FhirDataElement> findFhirDataElementsFromFutures(List<CompletableFuture<List<FhirDataElement>>> futures) {
        return futures.stream()
                .map(this::findDataFromFuture)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<FhirDataElement> findDataFromFuture(CompletableFuture<List<FhirDataElement>> f) {
        try {
            return f.get();
        } catch (Exception e) {
            log.error("Error with future get", e);
            return Collections.emptyList();
        }
    }

    public Set<String> collectQdmTypes(BonniePatient bonniePatient) {
        if (bonniePatient.getQdmPatient() == null || CollectionUtils.isEmpty(bonniePatient.getQdmPatient().getDataElements())) {
            log.warn("Bonnie Patient id: {} has no data elements", bonniePatient.get_id());
            return Collections.emptySet();
        } else {
            return bonniePatient.getQdmPatient().getDataElements().stream()
                    .map(QdmDataElement::get_type)
                    .collect(Collectors.toSet());
        }
    }

    private void processFuture(BonniePatient bonniePatient,
                               Patient fhirPatient,
                               ConverterBase<? extends IBaseResource> converter,
                               List<CompletableFuture<List<FhirDataElement>>> futures) {
        CompletableFuture<List<FhirDataElement>> future = converter.convertToFhirDataElement(bonniePatient, fhirPatient);
        future.orTimeout(THREAD_POOL_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        futures.add(future);
    }

    private ConversionResult findResultById(String id, List<ConversionResult> results) {
        return results.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No result found with the id: " + id));
    }
}
