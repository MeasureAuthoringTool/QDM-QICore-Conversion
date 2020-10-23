package gov.cms.mat.patients.conversion.conversion.helpers;

import gov.cms.mat.patients.conversion.conversion.ConverterBase;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ServiceRequest;

import java.util.ArrayList;
import java.util.List;

public interface ServiceRequestConverter extends DataElementFinder, FhirCreator {

    default QdmToFhirConversionResult convertToFhirServiceRequest(Patient fhirPatient,
                                                                  QdmDataElement qdmDataElement,
                                                                  ConverterBase<ServiceRequest> converterBase,
                                                                  ServiceRequest.ServiceRequestIntent intent) {
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setIntent(intent);

        List<String> conversionMessages = new ArrayList<>();

        serviceRequest.setId(qdmDataElement.get_id());
        serviceRequest.setSubject(createReference(fhirPatient));
        serviceRequest.setAuthoredOn(qdmDataElement.getAuthorDatetime());
        serviceRequest.setCode(convertToCodeSystems(converterBase.getCodeSystemEntriesService(), qdmDataElement.getDataElementCodes()));

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
