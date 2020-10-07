package gov.cms.mat.patients.conversion.conversion;


import ca.uhn.fhir.context.FhirContext;
import gov.cms.mat.patients.conversion.conversion.helpers.DataElementFinder;
import gov.cms.mat.patients.conversion.conversion.helpers.FhirCreator;
import gov.cms.mat.patients.conversion.dao.BonniePatient;
import gov.cms.mat.patients.conversion.dao.DataElements;
import gov.cms.mat.patients.conversion.dao.QdmCodeSystem;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Duration;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Quantity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MedicationDischargeConverter implements FhirCreator, DataElementFinder {
    private final CodeSystemEntriesService codeSystemEntriesService;
    private final FhirContext fhirContext;

    public MedicationDischargeConverter(CodeSystemEntriesService codeSystemEntriesService, FhirContext fhirContext) {
        this.codeSystemEntriesService = codeSystemEntriesService;
        this.fhirContext = fhirContext;
    }

    @Async("threadPoolConversion")
    public CompletableFuture<String> convertToString(BonniePatient bonniePatient, Patient fhirPatient) {
        List<MedicationRequest> medicationRequests = process(bonniePatient, fhirPatient);
        String json = manyToJson(fhirContext, medicationRequests);

        return CompletableFuture.completedFuture(json);
    }

    public List<MedicationRequest> process(BonniePatient bonniePatient, Patient fhirPatient) {
        List<DataElements> dataElements = findDataElementsByType(bonniePatient, "QDM::MedicationDischarge");

        if (dataElements.isEmpty()) {
            return Collections.emptyList();
        } else {
            return dataElements.stream()
                    .map(d -> convertToMedicationRequest(fhirPatient, d))
                    .collect(Collectors.toList());
        }
    }

    private MedicationRequest convertToMedicationRequest(Patient fhirPatient, DataElements dataElement) {
        MedicationRequest medicationRequest = new MedicationRequest();
        medicationRequest.setId(dataElement.get_id().getOid());
        medicationRequest.setSubject(createReference(fhirPatient));
        medicationRequest.setMedication(getMedicationCodeableConcept(dataElement.getDataElementCodes()));
        medicationRequest.setAuthoredOn(dataElement.getAuthorDatetime().getDate());

        medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.UNKNOWN);

        if (StringUtils.isNotBlank(dataElement.getRoute())) {
            //  medicationRequest.setDosageInstruction()
            log.info("We have a dosage");
        }

        if (dataElement.getDaysSupplied() != null) {
            MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest = medicationRequest.getDispenseRequest();
            Duration duration = new Duration();
            duration.setValue(dataElement.getDaysSupplied());
            dispenseRequest.setExpectedSupplyDuration(duration);
            log.info("We have a daysSupplied"); // all null in test data
        }

        if (StringUtils.isNotBlank(dataElement.getSupply())) {
            MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest = medicationRequest.getDispenseRequest();
            dispenseRequest.setQuantity(new Quantity(Long.parseLong(dataElement.getSupply()))); // could throw error
            log.info("We have a supply"); // all null in test data
        }

        if (StringUtils.isNotBlank(dataElement.getRefills())) {
            MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest = medicationRequest.getDispenseRequest();
            dispenseRequest.setNumberOfRepeatsAllowed(Integer.parseInt(dataElement.getRefills()));
            log.info("We have a refill"); // all null in test data
        }


        if (dataElement.getNegationRationale() != null) {
            medicationRequest.setDoNotPerform(true);
            CodeableConcept codeableConcept = convertToCodeableConcept(codeSystemEntriesService, dataElement.getNegationRationale());
            medicationRequest.setReasonCode(List.of(codeableConcept));
        }

        return medicationRequest;
    }

    private CodeableConcept getMedicationCodeableConcept(List<QdmCodeSystem> dataElementCodes) {
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding(createCodingFromDataElementCodes(codeSystemEntriesService, dataElementCodes));

        return codeableConcept;
    }
}
