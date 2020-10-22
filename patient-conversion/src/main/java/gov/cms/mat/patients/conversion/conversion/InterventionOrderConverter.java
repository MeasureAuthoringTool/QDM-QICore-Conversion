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
public class InterventionOrderConverter extends ConverterBase<ServiceRequest> implements ServiceRequestConverter {
    public static final String QDM_TYPE = "QDM::InterventionOrder";


    public InterventionOrderConverter(CodeSystemEntriesService codeSystemEntriesService,
                                      FhirContext fhirContext,
                                      ObjectMapper objectMapper,
                                      ValidationService validationService) {
        super(codeSystemEntriesService, fhirContext, objectMapper, validationService);
    }

    @Override
    String getQdmType() {
        return QDM_TYPE;
    }

    QdmToFhirConversionResult convertToFhir(Patient fhirPatient, QdmDataElement qdmDataElement) {
//        List<String> conversionMessages = new ArrayList<>();
//        ServiceRequest serviceRequest = new ServiceRequest();
//        serviceRequest.setId(qdmDataElement.get_id());
//        serviceRequest.setSubject(createReference(fhirPatient));
//        serviceRequest.setAuthoredOn(qdmDataElement.getAuthorDatetime());
//
//        // http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#81531-negation-rationale-for-intervention-order
//        //Constrain only to “order” (include children: original-order, reflex-order, filler-order, instance-order)
//        serviceRequest.setIntent(ServiceRequest.ServiceRequestIntent.ORDER);
//
//        CodeableConcept codeableConcept = convertToCodeSystems(codeSystemEntriesService, qdmDataElement.getDataElementCodes());
//        serviceRequest.setCode(codeableConcept); // ???  serviceRequest.setBasedOn()
//
//        if (!processNegation(qdmDataElement, serviceRequest)) {
//            serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.UNKNOWN);
//            conversionMessages.add(NO_STATUS_MAPPING);
//        }
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
