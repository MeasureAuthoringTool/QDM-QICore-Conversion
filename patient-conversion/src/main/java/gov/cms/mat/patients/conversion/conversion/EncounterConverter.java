package gov.cms.mat.patients.conversion.conversion;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.fhir.rest.dto.spreadsheet.CodeSystemEntry;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.dao.Diagnoses;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Duration;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
public class EncounterConverter extends ConverterBase<Encounter> {
    public static final String QDM_TYPE = "QDM::EncounterPerformed";

    public EncounterConverter(CodeSystemEntriesService codeSystemEntriesService,
                              FhirContext fhirContext,
                              ObjectMapper objectMapper) {
        super(codeSystemEntriesService, fhirContext, objectMapper);
    }

    @Override
    String getQdmType() {
        return QDM_TYPE;
    }

    Encounter convertToFhir(Patient fhirPatient, QdmDataElement dataElement) {
        Encounter encounter = new Encounter();
        encounter.setId(dataElement.get_id());

        encounter.setClass_(createCodingFromDataElementCodes(codeSystemEntriesService, dataElement.getDataElementCodes()));

        encounter.setStatus(Encounter.EncounterStatus.UNKNOWN);
        encounter.setPeriod(createFhirPeriod(dataElement));

        encounter.setDiagnosis(createDiagnoses(dataElement));

        encounter.setSubject(createReference(fhirPatient));

        if (dataElement.getLengthOfStay() == null) {
            log.debug("Length of stay is null");
        } else {
            Duration duration = new Duration();
            duration.setUnit(dataElement.getLengthOfStay().getUnit());
            duration.setValue(dataElement.getLengthOfStay().getValue());
            encounter.setLength(duration);
        }

        if (dataElement.getDischargeDisposition() != null) {
            Encounter.EncounterHospitalizationComponent hospitalizationComponent = encounter.getHospitalization();
            CodeableConcept codeableConcept = convertToCodeableConcept(codeSystemEntriesService, dataElement.getDischargeDisposition());
            hospitalizationComponent.setDischargeDisposition(codeableConcept);
        }

        return encounter;
    }


    private List<Encounter.DiagnosisComponent> createDiagnoses(QdmDataElement dataElement) {
        if (CollectionUtils.isEmpty(dataElement.getDiagnoses())) {
            return Collections.emptyList();
        } else {
            return dataElement.getDiagnoses().stream()
                    .map(this::createDiagnosis)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

    private Encounter.DiagnosisComponent createDiagnosis(Diagnoses diagnoses) {

        Encounter.DiagnosisComponent diagnosisComponent = new Encounter.DiagnosisComponent();

        try {
            CodeSystemEntry codeSystemEntry = codeSystemEntriesService.findRequired(diagnoses.getCode().getSystem());
            diagnosisComponent.setUse(createCodeableConcept(diagnoses.getCode(), codeSystemEntry.getUrl()));
        } catch (Exception e) {
            if( diagnoses.getCode() == null ) {
                log.warn("Diagnoses does not contain a code: {}", diagnoses);

                return null;
            } else {
                diagnosisComponent.setUse(createCodeableConcept(diagnoses.getCode(), diagnoses.getCode().getCodeSystem()));
            }
        }

        Reference condition = new Reference();
        diagnosisComponent.setCondition(condition);


        if (diagnoses.getRank() != null) {
            diagnosisComponent.setRank(diagnoses.getRank());
        }

        return diagnosisComponent;
    }
}
