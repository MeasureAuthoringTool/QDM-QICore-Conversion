package gov.cms.mat.patients.conversion.conversion;


import ca.uhn.fhir.context.FhirContext;
import gov.cms.mat.patients.conversion.conversion.helpers.DataElementFinder;
import gov.cms.mat.patients.conversion.conversion.helpers.FhirCreator;
import gov.cms.mat.patients.conversion.dao.BonniePatient;
import gov.cms.mat.patients.conversion.dao.DataElements;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Procedure;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InterventionPerformedConverter implements FhirCreator, DataElementFinder {
    private final CodeSystemEntriesService codeSystemEntriesService;
    private final FhirContext fhirContext;

    public InterventionPerformedConverter(CodeSystemEntriesService codeSystemEntriesService, FhirContext fhirContext) {
        this.codeSystemEntriesService = codeSystemEntriesService;
        this.fhirContext = fhirContext;
    }

    @Async("threadPoolConversion")
    public CompletableFuture<String> convertToString(BonniePatient bonniePatient, Patient fhirPatient) {
        List<Procedure> procedures = process(bonniePatient, fhirPatient);
        String json = manyToJson(fhirContext, procedures);

        return CompletableFuture.completedFuture(json);
    }

    public List<Procedure> process(BonniePatient bonniePatient, Patient fhirPatient) {
        List<DataElements> dataElements = findDataElementsByType(bonniePatient, "QDM::InterventionPerformed");

        if (dataElements.isEmpty()) {
            return Collections.emptyList();
        } else {
            return dataElements.stream()
                    .map(d -> convertToFhirEncounter(fhirPatient, d))
                    .collect(Collectors.toList());
        }
    }

    private Procedure convertToFhirEncounter(Patient fhirPatient, DataElements dataElements) {
        Procedure procedure = new Procedure();
        procedure.setId(dataElements.get_id().getOid());

        procedure.setSubject(createReference(fhirPatient));
        procedure.setStatus(Procedure.ProcedureStatus.UNKNOWN);

        procedure.setPerformed(createFhirPeriod(dataElements));
        /**
         * {
         * "dataTypeDescription": "Intervention, Performed",
         * "matAttributeName": "authorDatetime",
         * "fhirQicoreMapping": "ServiceRequest.authoredOn",
         * "fhirResource": "Procedure",
         * "fhirType": "dateTime",
         * "fhirElement": "authoredOn",
         * "helpWording": "Definition: When the request transitioned to being actionable.\n",
         * "dropDown": []
         * },
         */
        // procedure.setAuthoredOn todo how to map this


        if (StringUtils.isNotBlank(dataElements.getReason())) {
            // procedure.setReasonCode()
            log.info("We have Reason"); // all null in test data
        }

        CodeableConcept codeableConcept = convertToCodeSystems(codeSystemEntriesService, dataElements.getDataElementCodes());
        procedure.setCode(codeableConcept);

        return procedure;
    }


}
