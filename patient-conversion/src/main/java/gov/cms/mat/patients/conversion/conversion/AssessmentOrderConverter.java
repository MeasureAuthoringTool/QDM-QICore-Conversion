package gov.cms.mat.patients.conversion.conversion;


import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.helpers.ServiceRequestConverter;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AssessmentOrderConverter extends ConverterBase<ServiceRequest> implements ServiceRequestConverter {
    public static final String QDM_TYPE = "QDM::AssessmentOrder";

    public AssessmentOrderConverter(CodeSystemEntriesService codeSystemEntriesService,
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
    public QdmToFhirConversionResult<ServiceRequest> convertToFhir(Patient fhirPatient, QdmDataElement qdmDataElement) {
        return convertToFhirServiceRequest(fhirPatient,
                qdmDataElement,
                this,
                ServiceRequest.ServiceRequestIntent.ORDER);
    }

    @Override
    void convertNegation(QdmDataElement qdmDataElement, ServiceRequest serviceRequest) {
        convertNegationServiceRequest(qdmDataElement, serviceRequest);
    }
}
