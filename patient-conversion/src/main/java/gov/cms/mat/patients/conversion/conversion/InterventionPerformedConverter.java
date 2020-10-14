package gov.cms.mat.patients.conversion.conversion;


import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Procedure;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class InterventionPerformedConverter extends ConverterBase<Procedure> {
    public static final String QDM_TYPE = "QDM::InterventionPerformed";

    public InterventionPerformedConverter(CodeSystemEntriesService codeSystemEntriesService,
                                          FhirContext fhirContext,
                                          ObjectMapper objectMapper) {
        super(codeSystemEntriesService, fhirContext, objectMapper);
    }

    @Override
    String getQdmType() {
        return QDM_TYPE;
    }

    Procedure convertToFhir(Patient fhirPatient, QdmDataElement dataElements) {
        Procedure procedure = new Procedure();
        procedure.setId(dataElements.get_id());

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


        if (dataElements.getReason() != null) {
            procedure.setReasonCode(List.of(convertToCodeableConcept(codeSystemEntriesService, dataElements.getReason())));
        }

        CodeableConcept codeableConcept = convertToCodeSystems(codeSystemEntriesService, dataElements.getDataElementCodes());
        procedure.setCode(codeableConcept);

        return procedure;
    }


}
