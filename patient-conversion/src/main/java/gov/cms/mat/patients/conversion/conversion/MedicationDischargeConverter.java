package gov.cms.mat.patients.conversion.conversion;


import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.helpers.MedicationRequestConverter;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Duration;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MedicationDischargeConverter extends ConverterBase<MedicationRequest> implements MedicationRequestConverter {
    public static final String QDM_TYPE = "QDM::MedicationDischarge";

    public MedicationDischargeConverter(CodeSystemEntriesService codeSystemEntriesService,
                                        FhirContext fhirContext,
                                        ObjectMapper objectMapper,
                                        ValidationService validationService) {
        super(codeSystemEntriesService, fhirContext, objectMapper, validationService);
    }

    @Override
    public String getQdmType() {
        return QDM_TYPE;
    }

    @Override
    public QdmToFhirConversionResult<MedicationRequest> convertToFhir(Patient fhirPatient, QdmDataElement qdmDataElement) {
        QdmToFhirConversionResult<MedicationRequest> medicationRequestQdmToFhirConversionResult = convertToFhirMedicationRequest(fhirPatient,
                qdmDataElement,
                this,
                null);

        MedicationRequest medicationRequest = medicationRequestQdmToFhirConversionResult.getFhirResource();

        if (qdmDataElement.getDaysSupplied() != null) {
            MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest = medicationRequest.getDispenseRequest();
            Duration duration = new Duration();
            duration.setUnit("d");
            duration.setSystem("http://unitsofmeasure.org");
            duration.setValue(qdmDataElement.getDaysSupplied());
            dispenseRequest.setExpectedSupplyDuration(duration);
        }

        if (qdmDataElement.getRefills() != null) {
            MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest = medicationRequest.getDispenseRequest();
            dispenseRequest.setNumberOfRepeatsAllowed(qdmDataElement.getRefills());
        }

        return medicationRequestQdmToFhirConversionResult;

    }

    @Override
    void convertNegation(QdmDataElement qdmDataElement, MedicationRequest medicationRequest) {
        convertNegationMedicationRequest(qdmDataElement, medicationRequest);
    }
}
