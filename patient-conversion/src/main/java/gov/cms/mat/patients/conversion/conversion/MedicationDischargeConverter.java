package gov.cms.mat.patients.conversion.conversion;


import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.dao.QdmCodeSystem;
import gov.cms.mat.patients.conversion.exceptions.PatientConversionException;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Duration;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Quantity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MedicationDischargeConverter extends ConverterBase<MedicationRequest> {
    public static final String QDM_TYPE = "QDM::MedicationDischarge";

    public MedicationDischargeConverter(CodeSystemEntriesService codeSystemEntriesService,
                                        FhirContext fhirContext,
                                        ObjectMapper objectMapper) {
        super(codeSystemEntriesService, fhirContext, objectMapper);
    }

    @Override
    String getQdmType() {
        return QDM_TYPE;
    }

    MedicationRequest convertToFhir(Patient fhirPatient, QdmDataElement dataElement) {
        MedicationRequest medicationRequest = new MedicationRequest();
        medicationRequest.setId(dataElement.get_id());
        medicationRequest.setSubject(createReference(fhirPatient));
        medicationRequest.setMedication(getMedicationCodeableConcept(dataElement.getDataElementCodes()));
        medicationRequest.setAuthoredOn(dataElement.getAuthorDatetime());

        medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.UNKNOWN);

        if (dataElement.getRoute() != null) {
            //  medicationRequest.setDosageInstruction()
            // todo Still NO data
            log.info("We have a dosage");
        }

        if (dataElement.getDaysSupplied() != null) {
            MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest = medicationRequest.getDispenseRequest();
            Duration duration = new Duration();
            duration.setValue(dataElement.getDaysSupplied());
            dispenseRequest.setExpectedSupplyDuration(duration);
        }

        if (dataElement.getSupply() != null) {
            MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest = medicationRequest.getDispenseRequest();

            Quantity quantity = new Quantity();
            quantity.setValue(dataElement.getSupply().getValue());
            quantity.setSystem("http://unitsofmeasure.org");
            quantity.setCode(convertUnitToCode(dataElement.getSupply().getUnit()));
            dispenseRequest.setQuantity(quantity);

        }

        if (dataElement.getRefills() != null) {
            MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest = medicationRequest.getDispenseRequest();
            dispenseRequest.setNumberOfRepeatsAllowed(dataElement.getRefills());
        }


        if (dataElement.getNegationRationale() != null) {
            medicationRequest.setDoNotPerform(true);
            CodeableConcept codeableConcept = convertToCodeableConcept(codeSystemEntriesService, dataElement.getNegationRationale());
            medicationRequest.setReasonCode(List.of(codeableConcept));
        }

        return medicationRequest;
    }

    private String convertUnitToCode(String unit) {
        // https://ucum.nlm.nih.gov/ucum-lhc/demo.html Nice tool for codes
        // todo need all valid codes used in bonnie
        switch (unit) {
            case "days":
                return "d";
            default:
                throw new PatientConversionException("Cannot convert unit: " + unit + " to ucm code");
        }

    }

    private CodeableConcept getMedicationCodeableConcept(List<QdmCodeSystem> dataElementCodes) {
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding(createCodingFromDataElementCodes(codeSystemEntriesService, dataElementCodes));

        return codeableConcept;
    }
}
