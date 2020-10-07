package gov.cms.mat.patients.conversion.conversion;


import ca.uhn.fhir.context.FhirContext;
import gov.cms.mat.patients.conversion.conversion.helpers.DataElementFinder;
import gov.cms.mat.patients.conversion.conversion.helpers.FhirCreator;
import gov.cms.mat.patients.conversion.dao.BonniePatient;
import gov.cms.mat.patients.conversion.dao.DataElements;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InterventionOrderConverter implements FhirCreator, DataElementFinder {
    private final CodeSystemEntriesService codeSystemEntriesService;
    private final FhirContext fhirContext;

    public InterventionOrderConverter(CodeSystemEntriesService codeSystemEntriesService, FhirContext fhirContext) {
        this.codeSystemEntriesService = codeSystemEntriesService;
        this.fhirContext = fhirContext;
    }

    @Async("threadPoolConversion")
    public CompletableFuture<String> convertToString(BonniePatient bonniePatient, Patient fhirPatient) {
        List<ServiceRequest> serviceRequests = process(bonniePatient, fhirPatient);
        String json = manyToJson(fhirContext, serviceRequests);

        return CompletableFuture.completedFuture(json);
    }

    public List<ServiceRequest> process(BonniePatient bonniePatient, Patient fhirPatient) {
        List<DataElements> dataElements = findDataElementsByType(bonniePatient, "QDM::InterventionOrder");

        if (dataElements.isEmpty()) {
            return Collections.emptyList();
        } else {
            return dataElements.stream()
                    .map(d -> convertToFhirEncounter(fhirPatient, d))
                    .collect(Collectors.toList());
        }
    }

    private ServiceRequest convertToFhirEncounter(Patient fhirPatient, DataElements dataElements) {
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setId(dataElements.get_id().getOid());

        serviceRequest.setSubject(createReference(fhirPatient));
        serviceRequest.setAuthoredOn(dataElements.getAuthorDatetime().getDate());

        serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.UNKNOWN);

        CodeableConcept codeableConcept = convertToCodeSystems(codeSystemEntriesService, dataElements.getDataElementCodes());
        serviceRequest.setCode(codeableConcept); // ???  serviceRequest.setBasedOn()

        return serviceRequest;
    }
}
