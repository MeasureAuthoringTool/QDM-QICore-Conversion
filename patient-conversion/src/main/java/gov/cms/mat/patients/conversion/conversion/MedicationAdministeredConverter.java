package gov.cms.mat.patients.conversion.conversion;


import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.MedicationAdministration;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class MedicationAdministeredConverter extends ConverterBase<MedicationAdministration> {
    public static final String QDM_TYPE = "QDM::MedicationAdministered";

    public MedicationAdministeredConverter(CodeSystemEntriesService codeSystemEntriesService,
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
    public QdmToFhirConversionResult<MedicationAdministration> convertToFhir(Patient fhirPatient, QdmDataElement qdmDataElement) {
        List<String> conversionMessages = new ArrayList<>();

        MedicationAdministration medicationAdministration = new MedicationAdministration();
        medicationAdministration.setId(qdmDataElement.get_id());
        medicationAdministration.setSubject(createReference(fhirPatient));

        medicationAdministration.setMedication(convertToCodeSystems(codeSystemEntriesService, qdmDataElement.getDataElementCodes()));

        if (qdmDataElement.getRelevantPeriod() != null) {
            medicationAdministration.setEffective(createFhirPeriod(qdmDataElement.getRelevantPeriod()));
        }

        if (!processNegation(qdmDataElement, medicationAdministration)) {
            medicationAdministration.setStatus("unknown");
            conversionMessages.add(NO_STATUS_MAPPING);
        }

        return QdmToFhirConversionResult.<MedicationAdministration>builder()
                .fhirResource(medicationAdministration)
                .conversionMessages(conversionMessages)
                .build();

    }

    @Override
    void convertNegation(QdmDataElement qdmDataElement, MedicationAdministration medicationAdministration) {
        medicationAdministration.setStatus("not-done");

        CodeableConcept codeableConcept = convertToCodeableConcept(codeSystemEntriesService, qdmDataElement.getNegationRationale());
        medicationAdministration.setStatusReason(List.of(codeableConcept));

        if (qdmDataElement.getAuthorDatetime() != null) {
            Extension extension = new Extension(QICORE_RECORDED);
            extension.setValue(new DateTimeType(qdmDataElement.getAuthorDatetime()));
            medicationAdministration.setExtension(List.of(extension));
        }
    }
}
