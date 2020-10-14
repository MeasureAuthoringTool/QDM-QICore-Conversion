package gov.cms.mat.patients.conversion.conversion;


import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InterventionOrderConverter extends ConverterBase<ServiceRequest> {
    public static final String QDM_TYPE = "QDM::InterventionOrder";

    public InterventionOrderConverter(CodeSystemEntriesService codeSystemEntriesService,
                                      FhirContext fhirContext,
                                      ObjectMapper objectMapper) {
        super(codeSystemEntriesService, fhirContext, objectMapper);
    }

    @Override
    String getQdmType() {
        return QDM_TYPE;
    }

    ServiceRequest convertToFhir(Patient fhirPatient, QdmDataElement dataElements) {
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setId(dataElements.get_id());

        serviceRequest.setSubject(createReference(fhirPatient));
        serviceRequest.setAuthoredOn(dataElements.getAuthorDatetime());

        serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.UNKNOWN);

        CodeableConcept codeableConcept = convertToCodeSystems(codeSystemEntriesService, dataElements.getDataElementCodes());
        serviceRequest.setCode(codeableConcept); // ???  serviceRequest.setBasedOn()

        return serviceRequest;
    }
}
