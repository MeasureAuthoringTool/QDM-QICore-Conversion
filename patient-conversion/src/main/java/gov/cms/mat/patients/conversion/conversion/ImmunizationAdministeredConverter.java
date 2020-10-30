package gov.cms.mat.patients.conversion.conversion;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.DateTimeType;
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
        List<String> conversionMessages = new ArrayList<>();

        Immunization immunization = new Immunization();

        // http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#8131-immunization-administered
        // Constrain to Completed, entered-in-error, not-done
        immunization.setStatus(Immunization.ImmunizationStatus.NULL);
        conversionMessages.add(NO_STATUS_MAPPING);

        immunization.setVaccineCode(convertToCodeSystems(getCodeSystemEntriesService(), qdmDataElement.getDataElementCodes()));

        immunization.setId(qdmDataElement.get_id());

        if (qdmDataElement.getDosage() != null) {
            immunization.setDoseQuantity(convertQuantity(qdmDataElement.getDosage()));
        }

        processNegation(qdmDataElement, immunization);

        if (qdmDataElement.getRoute() != null) {
            immunization.setRoute(convertToCodeableConcept(codeSystemEntriesService, qdmDataElement.getRoute()));
        }

//        if (qdmDataElement.getReason() != null) {
//            // No data todo can we expect data
//            immunization.setReasonCode(List.of(convertToCodeableConcept(codeSystemEntriesService, qdmDataElement.getReason())));
//        }

        if (qdmDataElement.getRelevantDatetime() != null) {
            immunization.setOccurrence(new DateTimeType(qdmDataElement.getRelevantDatetime()));
        }

        immunization.setRecorded(qdmDataElement.getAuthorDatetime());

//        if( qdmDataElement.getPerformer() != null) {
//             // No data todo can we expect data
//        }

        immunization.setPatient(createReference(fhirPatient));

        return QdmToFhirConversionResult.<Immunization>builder()
                .fhirResource(immunization)
                .conversionMessages(conversionMessages)
                .build();
    }

    @Override
    void convertNegation(QdmDataElement qdmDataElement, Immunization immunization) {
        // http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#8131-immunization-administered
        log.warn("QdmDataElement id: {} - for converting {} to Fhir Immunization,  Will create a MedicationRequest in another class.",
                qdmDataElement.get_id(), qdmDataElement.get_type());
    }
}
