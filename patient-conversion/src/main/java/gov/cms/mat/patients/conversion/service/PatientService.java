package gov.cms.mat.patients.conversion.service;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.EncounterConverter;
import gov.cms.mat.patients.conversion.conversion.InterventionOrderConverter;
import gov.cms.mat.patients.conversion.conversion.InterventionPerformedConverter;
import gov.cms.mat.patients.conversion.conversion.MedicationDischargeConverter;
import gov.cms.mat.patients.conversion.conversion.PatientConverter;
import gov.cms.mat.patients.conversion.conversion.helpers.FhirCreator;
import gov.cms.mat.patients.conversion.dao.BonniePatient;
import gov.cms.mat.patients.conversion.data.ConversionResult;
import gov.cms.mat.patients.conversion.exceptions.PatientConversionException;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PatientService implements FhirCreator {
    private static final int THREAD_POOL_TIMEOUT_MINUTES = 1;
    private final ObjectMapper mapper = new ObjectMapper();
    private final PatientConverter patientConverter;
    private final EncounterConverter encounterConverter;
    private final InterventionOrderConverter interventionOrderConverter;
    private final InterventionPerformedConverter interventionPerformedConverter;
    private final MedicationDischargeConverter medicationDischargeConverter;

    private final FhirContext fhirContext;

    public PatientService(PatientConverter patientConverter,
                          EncounterConverter encounterConverter,
                          InterventionOrderConverter interventionOrderConverter,
                          InterventionPerformedConverter interventionPerformedConverter,
                          MedicationDischargeConverter medicationDischargeConverter,
                          FhirContext fhirContext) {
        this.patientConverter = patientConverter;
        this.encounterConverter = encounterConverter;
        this.interventionOrderConverter = interventionOrderConverter;
        this.interventionPerformedConverter = interventionPerformedConverter;
        this.medicationDischargeConverter = medicationDischargeConverter;
        this.fhirContext = fhirContext;
    }

    public List<ConversionResult> processMany( List<BonniePatient> bonniePatients) {
        List<ConversionResult> results = bonniePatients.parallelStream()
                .map(this::processOne)
                .collect(Collectors.toList());

        //  maintain the order from the input list
        return bonniePatients.stream()
                .map(b -> findResultById(b.get_id().getOid(), results))
                .collect(Collectors.toList());
    }

    public ConversionResult processOne(BonniePatient bonniePatient) {
        try {
            @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
            List<CompletableFuture<List<String>>> futures = new ArrayList<>();

            Patient fhirPatient = patientConverter.process(bonniePatient);

            CompletableFuture<String> encounters = encounterConverter.convertToString(bonniePatient, fhirPatient);
            encounters.orTimeout(THREAD_POOL_TIMEOUT_MINUTES, TimeUnit.MINUTES);

            CompletableFuture<String> serviceRequests = interventionOrderConverter.convertToString(bonniePatient, fhirPatient);
            serviceRequests.orTimeout(THREAD_POOL_TIMEOUT_MINUTES, TimeUnit.MINUTES);

            CompletableFuture<String> procedures = interventionPerformedConverter.convertToString(bonniePatient, fhirPatient);
            procedures.orTimeout(THREAD_POOL_TIMEOUT_MINUTES, TimeUnit.MINUTES);

            CompletableFuture<String> medicationRequests = medicationDischargeConverter.convertToString(bonniePatient, fhirPatient);
            medicationRequests.orTimeout(THREAD_POOL_TIMEOUT_MINUTES, TimeUnit.MINUTES);

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

            return ConversionResult.builder()
                    .patientId(bonniePatient.get_id().getOid())
                    .fhirPatient(mapper.readTree(toJson(fhirContext, fhirPatient)))
                    .encounters(mapper.readTree(encounters.get()))
                    .serviceRequests(mapper.readTree(serviceRequests.get()))
                    .procedures(mapper.readTree(procedures.get()))
                    .medicationRequests(mapper.readTree(medicationRequests.get()))
                    .build();
        } catch (Exception e) {
            log.warn("Error ", e);
            throw new PatientConversionException("Error");
        }
    }

    private ConversionResult findResultById(String id, List<ConversionResult> results) {
        return results.stream()
                .filter(r -> r.getPatientId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No result found with the id: " + id));
    }
}
