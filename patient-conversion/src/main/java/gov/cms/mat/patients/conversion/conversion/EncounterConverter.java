package gov.cms.mat.patients.conversion.conversion;

import ca.uhn.fhir.context.FhirContext;
import gov.cms.mat.fhir.rest.dto.spreadsheet.CodeSystemEntry;
import gov.cms.mat.patients.conversion.conversion.helpers.DataElementFinder;
import gov.cms.mat.patients.conversion.conversion.helpers.FhirCreator;
import gov.cms.mat.patients.conversion.dao.BonniePatient;
import gov.cms.mat.patients.conversion.dao.DataElements;
import gov.cms.mat.patients.conversion.dao.Diagnoses;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Duration;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class EncounterConverter implements FhirCreator, DataElementFinder {
    private final CodeSystemEntriesService codeSystemEntriesService;
    private final FhirContext fhirContext;

    public EncounterConverter(CodeSystemEntriesService codeSystemEntriesService, FhirContext fhirContext) {
        this.codeSystemEntriesService = codeSystemEntriesService;
        this.fhirContext = fhirContext;
    }

    @Async("threadPoolConversion")
    public CompletableFuture<String> convertToString(BonniePatient bonniePatient, Patient fhirPatient) {
        List<Encounter> encounters = process(bonniePatient, fhirPatient);
        String json = manyToJson(fhirContext, encounters);

        return CompletableFuture.completedFuture(json);
    }

    public List<Encounter> process(BonniePatient bonniePatient, Patient fhirPatient) {
        List<DataElements> dataElements = findDataElementsByType(bonniePatient, "QDM::EncounterPerformed");

        if (dataElements.isEmpty()) {
            return Collections.emptyList();
        } else {
            return dataElements.stream()
                    .map(d -> convertToFhirEncounter(fhirPatient, d))
                    .collect(Collectors.toList());
        }
    }

    private Encounter convertToFhirEncounter(Patient fhirPatient, DataElements dataElement) {
        Encounter encounter = new Encounter();
        encounter.setId(dataElement.get_id().getOid());

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

    private List<Encounter.DiagnosisComponent> createDiagnoses(DataElements dataElement) {
        if (CollectionUtils.isEmpty(dataElement.getDiagnoses())) {
            return Collections.emptyList();
        } else {
            return dataElement.getDiagnoses().stream()
                    .map(this::createDiagnosis)
                    .collect(Collectors.toList());
        }
    }

    private Encounter.DiagnosisComponent createDiagnosis(Diagnoses diagnoses) {
        Encounter.DiagnosisComponent diagnosisComponent = new Encounter.DiagnosisComponent();
        CodeSystemEntry codeSystemEntry = codeSystemEntriesService.findRequired(diagnoses.getCode().getSystem());

        Reference condition = new Reference();
        // condition.setDisplay(bonniePatient.getNotes()); //  put here??
        diagnosisComponent.setCondition(condition);

        diagnosisComponent.setUse(createCodeableConcept(diagnoses.getCode(), codeSystemEntry.getUrl()));

        diagnosisComponent.setRank(diagnoses.getRank());

        return diagnosisComponent;
    }
}
