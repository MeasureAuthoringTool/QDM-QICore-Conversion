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
    String getQdmType() {
        return QDM_TYPE;
    }

    @Override
    QdmToFhirConversionResult convertToFhir(Patient fhirPatient, QdmDataElement qdmDataElement) {
//        List<String> conversionMessages = new ArrayList<>();
//        ServiceRequest serviceRequest = new ServiceRequest();
//        serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.UNKNOWN);
//        conversionMessages.add(NO_STATUS_MAPPING);
//
//        // http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#841-assessment-order
//        // Constrain only to “order” (include children: original-order, reflex-order, filler-order, instance-order)
//        serviceRequest.setIntent(ServiceRequest.ServiceRequestIntent.ORDER);
//
//        serviceRequest.setId(qdmDataElement.get_id());
//
//        serviceRequest.setSubject(createReference(fhirPatient));
//
//        serviceRequest.setAuthoredOn(qdmDataElement.getAuthorDatetime());
//
//        serviceRequest.setCode(convertToCodeSystems(codeSystemEntriesService, qdmDataElement.getDataElementCodes()));
//
//        processNegation(qdmDataElement, serviceRequest);
//
//        return QdmToFhirConversionResult.builder()
//                .fhirResource(serviceRequest)
//                .conversionMessages(conversionMessages)
//                .build();
        return convertServiceRequestToFhir(fhirPatient, qdmDataElement, codeSystemEntriesService, this);
    }

    @Override
    void convertNegation(QdmDataElement qdmDataElement, ServiceRequest serviceRequest) {
        convertNegationServiceRequest(qdmDataElement, serviceRequest);
    }
}
