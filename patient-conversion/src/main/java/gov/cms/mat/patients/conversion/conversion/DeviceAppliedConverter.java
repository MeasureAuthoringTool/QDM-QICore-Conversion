package gov.cms.mat.patients.conversion.conversion;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Procedure;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class DeviceAppliedConverter extends ConverterBase<Procedure> {

    public static final String QDM_TYPE = "QDM::DeviceApplied";

    public DeviceAppliedConverter(CodeSystemEntriesService codeSystemEntriesService,
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
        Procedure procedure = new Procedure();

        procedure.setId(qdmDataElement.get_id());
        procedure.setStatus(Procedure.ProcedureStatus.UNKNOWN);
        procedure.setSubject(createReference(fhirPatient));
        procedure.setPerformed(createFhirPeriod(qdmDataElement.getRelevantPeriod()));

        conversionMessages.add(NO_STATUS_MAPPING);

        //Todo difference between Procedure.usedCode and Procedure.code
        procedure.setCode(convertToCodeSystems(codeSystemEntriesService, qdmDataElement.getDataElementCodes()));

        if (qdmDataElement.getReason() != null) {
            procedure.setReasonCode(List.of(convertToCodeableConcept(codeSystemEntriesService, qdmDataElement.getReason())));
        }

        if (!processNegation(qdmDataElement, procedure)) {
            procedure.setStatus(Procedure.ProcedureStatus.COMPLETED);
        }

        return QdmToFhirConversionResult.builder()
                .fhirResource(procedure)
                .conversionMessages(conversionMessages)
                .build();
    }

    @Override
    void convertNegation(QdmDataElement qdmDataElement, Procedure procedure) {
        convertNegationProcedure(qdmDataElement, procedure);
    }
}
