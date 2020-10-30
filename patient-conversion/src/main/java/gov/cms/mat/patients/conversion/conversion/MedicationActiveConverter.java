package gov.cms.mat.patients.conversion.conversion;


import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.helpers.MedicationRequestConverter;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MedicationActiveConverter extends ConverterBase<MedicationRequest> implements MedicationRequestConverter {
    public static final String QDM_TYPE = "QDM::MedicationActive";

    public MedicationActiveConverter(CodeSystemEntriesService codeSystemEntriesService,
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
        var result = convertToFhirMedicationRequest(fhirPatient,
                qdmDataElement,
                this,
                MedicationRequest.MedicationRequestIntent.ORDER,
                false);

        //MedicationRequest.status	Constrain to “active”
        result.getFhirResource().setStatus(MedicationRequest.MedicationRequestStatus.ACTIVE);

        return result;
    }
}

