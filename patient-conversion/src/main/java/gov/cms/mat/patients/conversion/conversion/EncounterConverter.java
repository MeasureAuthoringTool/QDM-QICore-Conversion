package gov.cms.mat.patients.conversion.conversion;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.fhir.rest.dto.spreadsheet.CodeSystemEntry;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.Diagnoses;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Duration;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
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
                              ObjectMapper objectMapper,
                              ValidationService validationService) {
        super(codeSystemEntriesService, fhirContext, objectMapper, validationService);
    }

    @Override
    public String getQdmType() {
        return QDM_TYPE;
    }

    @Override
    public QdmToFhirConversionResult<Encounter> convertToFhir(Patient fhirPatient, QdmDataElement qdmDataElement) {
        List<String> conversionMessages = new ArrayList<>();
        Encounter encounter = new Encounter();
        encounter.setId(qdmDataElement.get_id());

        encounter.setClass_(createCodingFromDataElementCodes(codeSystemEntriesService, qdmDataElement.getDataElementCodes()));

        //http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#8114-encounter-performed
        // 	consider constraint to - arrived, triaged, in-progress, on-leave, finished
        encounter.setStatus(Encounter.EncounterStatus.UNKNOWN);
        conversionMessages.add(NO_STATUS_MAPPING);
        encounter.setPeriod(convertPeriod(qdmDataElement.getRelevantPeriod()));

        encounter.setDiagnosis(createDiagnoses(qdmDataElement));

        encounter.setSubject(createReference(fhirPatient));

        if (qdmDataElement.getLengthOfStay() == null) {
            log.debug("Length of stay is null");
        } else {
            Duration duration = new Duration();
            duration.setUnit(qdmDataElement.getLengthOfStay().getUnit());
            duration.setValue(qdmDataElement.getLengthOfStay().getValue());
            encounter.setLength(duration);
        }

        if (qdmDataElement.getDischargeDisposition() != null) {
            Encounter.EncounterHospitalizationComponent hospitalizationComponent = encounter.getHospitalization();
            CodeableConcept codeableConcept = convertToCodeableConcept(codeSystemEntriesService, qdmDataElement.getDischargeDisposition());
            hospitalizationComponent.setDischargeDisposition(codeableConcept);
        }

        if (processNegation(qdmDataElement, encounter)) {
            //todo stan we have many with data right now
            // http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#8114-encounter-performed
            //	There is no current use case for an eCQM to request a reason for failure to perform an encounter.

            conversionMessages.add("There is no current use case for an eCQM to request a reason for failure to perform an encounter.");

        }

        return QdmToFhirConversionResult.<Encounter>builder()
                .fhirResource(encounter)
                .conversionMessages(conversionMessages)
                .build();
    }


    @Override
    void convertNegation(QdmDataElement qdmDataElement, Encounter encounter) {
        // nothing to do
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
            if (diagnoses.getCode() == null) {
                log.warn("Diagnoses does not contain a code: {}", diagnoses);

                return null;
            } else {
                diagnosisComponent.setUse(createCodeableConcept(diagnoses.getCode(), diagnoses.getCode().getCodeSystem()));
            }
        }

        //  todo how to set
        //   "message": "Profile http://hl7.org/fhir/StructureDefinition/Encounter, Element 'Encounter.diagnosis[0].condition': minimum required = 1, but only found 0",

        //  Reference condition = new Reference();
        //  diagnosisComponent.setCondition(condition);


        if (diagnoses.getRank() != null) {
            diagnosisComponent.setRank(diagnoses.getRank());
        }

        return diagnosisComponent;
    }
}
