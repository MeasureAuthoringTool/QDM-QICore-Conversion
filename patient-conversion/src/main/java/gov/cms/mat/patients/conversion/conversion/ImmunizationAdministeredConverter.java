package gov.cms.mat.patients.conversion.conversion;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ImmunizationAdministeredConverter extends ConverterBase<Immunization> {

    public static final String QDM_TYPE = "QDM::ImmunizationAdministered";

    public ImmunizationAdministeredConverter(CodeSystemEntriesService codeSystemEntriesService,
                                FhirContext fhirContext,
                                ObjectMapper objectMapper,
                                ValidationService validationService) {
        super(codeSystemEntriesService, fhirContext, objectMapper, validationService);
    }

    @Override
    public String getQdmType() {
        return QDM_TYPE;
    }

    @Override
    public QdmToFhirConversionResult<Immunization> convertToFhir(Patient fhirPatient, QdmDataElement qdmDataElement) {
        Immunization immunization = new Immunization();
        List<String> conversionMessages = new ArrayList<>();

        immunization.setPatient(createReference(fhirPatient));
        immunization.setId(qdmDataElement.get_id());
//        Dosage	Immunization.doseQuantity
//        immunization.setDoseQuantity(qdmDataElement.getDosage());
        immunization.setVaccineCode(convertToCodeSystems(getCodeSystemEntriesService(), qdmDataElement.getDataElementCodes()));
        if(qdmDataElement.getReason() != null) {
            immunization.setReasonCode(List.of(convertToCodeableConcept(codeSystemEntriesService, qdmDataElement.getReason())));
        }

//        Route	Immunization.route
//        Relevant dateTime	Immunization.occurrence[x]
//        author dateTime	Immunization.recorded
//        Performer	Immunization.performer.actor

        //Todo Negation for Immunization Administered is of type MedicationRequest.
        /*if (!processNegation(qdmDataElement, immunization)) {
            // http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#8131-immunization-administered
            // 	Constrain to active, completed, on-hold
            immunization.setStatus(Immunization.ImmunizationStatus.COMPLETED);
            conversionMessages.add(NO_STATUS_MAPPING);
        }*/

        return QdmToFhirConversionResult.<Immunization>builder()
                .fhirResource(immunization)
                .conversionMessages(conversionMessages)
                .build();
    }
}
