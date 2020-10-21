package gov.cms.mat.patients.conversion.conversion;


import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class DiagnosticStudyOrderConverter extends ConverterBase<ServiceRequest> {
    public static final String QDM_TYPE = "QDM::DiagnosticStudyOrder";

    public DiagnosticStudyOrderConverter(CodeSystemEntriesService codeSystemEntriesService,
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
    QdmToFhirConversionResult convertToFhir(Patient fhirPatient, QdmDataElement qdmDataElement) {
        List<String> conversionMessages = new ArrayList<>();

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setId(qdmDataElement.get_id());
        serviceRequest.setSubject(createReference(fhirPatient));

        serviceRequest.setAuthoredOn(qdmDataElement.getAuthorDatetime());

        serviceRequest.setCode(convertToCodeSystems(codeSystemEntriesService, qdmDataElement.getDataElementCodes()));

        if (!processNegation(qdmDataElement, serviceRequest)) {
            //http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#8101-diagnostic-study-order
            //Constrain to one or more of active, on-hold, completed
            serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.UNKNOWN);
            conversionMessages.add(NO_STATUS_MAPPING);
        }

        return QdmToFhirConversionResult.builder()
                .fhirResource(serviceRequest)
                .conversionMessages(conversionMessages)
                .build();
    }

    @Override
    void convertNegation(QdmDataElement qdmDataElement, ServiceRequest serviceRequest) {
        convertNegationServiceRequest(qdmDataElement, serviceRequest);
    }
}
