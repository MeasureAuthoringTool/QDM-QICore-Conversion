package gov.cms.mat.patients.conversion.conversion;


import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class InterventionOrderConverter extends ConverterBase<ServiceRequest> {
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
        List<String> conversionMessages = new ArrayList<>();
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setId(qdmDataElement.get_id());
        serviceRequest.setSubject(createReference(fhirPatient));
        serviceRequest.setAuthoredOn(qdmDataElement.getAuthorDatetime());

        // http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#81531-negation-rationale-for-intervention-order
        //Constrain only to “order” (include children: original-order, reflex-order, filler-order, instance-order)
        serviceRequest.setIntent(ServiceRequest.ServiceRequestIntent.ORDER);


        CodeableConcept codeableConcept = convertToCodeSystems(codeSystemEntriesService, qdmDataElement.getDataElementCodes());
        serviceRequest.setCode(codeableConcept); // ???  serviceRequest.setBasedOn()

        if (qdmDataElement.getNegationRationale() != null) {
            serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.COMPLETED);

            serviceRequest.setDoNotPerform(true);

            Extension extensionDoNotPerformReason = new Extension(QICORE_DO_NOT_PERFORM_REASON);
            extensionDoNotPerformReason.setValue(convertToCoding(codeSystemEntriesService, qdmDataElement.getNegationRationale()));
            serviceRequest.setExtension(List.of(extensionDoNotPerformReason));
        } else {
            serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.UNKNOWN);
            conversionMessages.add(NO_STATUS_MAPPING);
        }

        return QdmToFhirConversionResult.builder()
                .fhirResource(serviceRequest)
                .conversionMessages(conversionMessages)
                .build();
    }
}
