package gov.cms.mat.patients.conversion.conversion.helpers;

import gov.cms.mat.patients.conversion.conversion.ConverterBase;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ServiceRequest;

import java.util.ArrayList;
import java.util.List;

public interface ServiceRequestConverter extends DataElementFinder, FhirCreator {

    default QdmToFhirConversionResult convertServiceRequestToFhir(Patient fhirPatient,
                                                    QdmDataElement qdmDataElement,
                                                    CodeSystemEntriesService codeSystemEntriesService,
                                                    ConverterBase<ServiceRequest> converterBase) {
        List<String> conversionMessages = new ArrayList<>();
        ServiceRequest serviceRequest = new ServiceRequest();

        serviceRequest.setId(qdmDataElement.get_id());
        serviceRequest.setSubject(createReference(fhirPatient));
        serviceRequest.setAuthoredOn(qdmDataElement.getAuthorDatetime());

        serviceRequest.setIntent(ServiceRequest.ServiceRequestIntent.ORDER);
        serviceRequest.setCode(convertToCodeSystems(codeSystemEntriesService, qdmDataElement.getDataElementCodes()));

        if (!converterBase.processNegation(qdmDataElement, serviceRequest)) {
            //http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#892-device-order--non-patient-use-devices
            //Constrain to one or more of active, on-hold, completed
            serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.UNKNOWN);
            conversionMessages.add(ConverterBase.NO_STATUS_MAPPING);
        }

        return QdmToFhirConversionResult.builder()
                .fhirResource(serviceRequest)
                .conversionMessages(conversionMessages)
                .build();
    }
}
