package gov.cms.mat.patients.conversion.conversion;


import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmCodeSystem;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.exceptions.PatientConversionException;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Duration;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Quantity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class MedicationDischargeConverter extends ConverterBase<MedicationRequest> {
    public static final String QDM_TYPE = "QDM::MedicationDischarge";

    public MedicationDischargeConverter(CodeSystemEntriesService codeSystemEntriesService,
                                        FhirContext fhirContext,
                                        ObjectMapper objectMapper,
                                        ValidationService validationService) {
        super(codeSystemEntriesService, fhirContext, objectMapper, validationService);
    }

    @Override
    String getQdmType() {
        return QDM_TYPE;
    }

    @Override
    QdmToFhirConversionResult<MedicationRequest> convertToFhir(Patient fhirPatient, QdmDataElement qdmDataElement) {
        List<String> conversionMessages = new ArrayList<>();

        MedicationRequest medicationRequest = new MedicationRequest();
        medicationRequest.setId(qdmDataElement.get_id());
        medicationRequest.setSubject(createReference(fhirPatient));
        medicationRequest.setMedication(getMedicationCodeableConcept(qdmDataElement.getDataElementCodes()));
        medicationRequest.setAuthoredOn(qdmDataElement.getAuthorDatetime());

        // medicationRequest.setIntent()

        if (qdmDataElement.getRoute() != null) {
            //  medicationRequest.setDosageInstruction()
            // todo Still NO data
            log.info("We have a dosage");
        }

        if (qdmDataElement.getDaysSupplied() != null) {
            MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest = medicationRequest.getDispenseRequest();
            Duration duration = new Duration();
            duration.setValue(qdmDataElement.getDaysSupplied());
            dispenseRequest.setExpectedSupplyDuration(duration);
        }

        if (qdmDataElement.getSupply() != null) {
            MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest = medicationRequest.getDispenseRequest();

            Quantity quantity = new Quantity();
            quantity.setValue(qdmDataElement.getSupply().getValue());
            quantity.setSystem("http://unitsofmeasure.org");

            try {
                quantity.setCode(convertUnitToCode(qdmDataElement.getSupply().getUnit()));
            } catch (PatientConversionException e) {
                conversionMessages.add(e.getMessage());
            }

            dispenseRequest.setQuantity(quantity);
        }

        if (qdmDataElement.getRefills() != null) {
            MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest = medicationRequest.getDispenseRequest();
            dispenseRequest.setNumberOfRepeatsAllowed(qdmDataElement.getRefills());
        }

        if (!processNegation(qdmDataElement, medicationRequest)) {
            // http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#8173-medication-discharge
            // 	Constrain to active, completed, on-hold
            medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.UNKNOWN);
            conversionMessages.add(NO_STATUS_MAPPING);
        }

        return QdmToFhirConversionResult.<MedicationRequest>builder()
                .fhirResource(medicationRequest)
                .conversionMessages(conversionMessages)
                .build();
    }


    private CodeableConcept getMedicationCodeableConcept(List<QdmCodeSystem> dataElementCodes) {
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding(createCodingFromDataElementCodes(codeSystemEntriesService, dataElementCodes));

        return codeableConcept;
    }

    @Override
    void convertNegation(QdmDataElement qdmDataElement, MedicationRequest medicationRequest) {
        medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.COMPLETED);

        medicationRequest.setDoNotPerform(true);
        CodeableConcept codeableConcept = convertToCodeableConcept(codeSystemEntriesService, qdmDataElement.getNegationRationale());
        medicationRequest.setReasonCode(List.of(codeableConcept));
    }
}
