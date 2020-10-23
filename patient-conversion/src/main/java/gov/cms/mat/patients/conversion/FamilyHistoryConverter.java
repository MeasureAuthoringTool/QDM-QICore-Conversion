package gov.cms.mat.patients.conversion;


import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.ConverterBase;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.FamilyMemberHistory;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class FamilyHistoryConverter extends ConverterBase<FamilyMemberHistory> {
    public static final String QDM_TYPE = "QDM::FamilyHistory";

    public FamilyHistoryConverter(CodeSystemEntriesService codeSystemEntriesService,
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
    public QdmToFhirConversionResult<FamilyMemberHistory> convertToFhir(Patient fhirPatient, QdmDataElement qdmDataElement) {
        List<String> conversionMessages = new ArrayList<>();

        FamilyMemberHistory familyMemberHistory = new FamilyMemberHistory();
        familyMemberHistory.setId(qdmDataElement.get_id());
        familyMemberHistory.setPatient(createReference(fhirPatient));
        familyMemberHistory.setDate(qdmDataElement.getAuthorDatetime());

        //http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#812-family-history
        //Constrain to Completed, entered-in-error, not-done
        familyMemberHistory.setStatus(FamilyMemberHistory.FamilyHistoryStatus.NULL);
        conversionMessages.add(NO_STATUS_MAPPING);

        processNegation(qdmDataElement, familyMemberHistory); // should never have any

        if (qdmDataElement.getRelationship() != null) {
            //https://terminology.hl7.org/1.0.0/CodeSystem-v3-RoleCode.html
            // we only have AUNT as an example, we could convert if new the input set and no system
            if (qdmDataElement.getRelationship().getSystem() == null) {
                conversionMessages.add("Relation for code " + qdmDataElement.getRelationship().getCode() + " has no system");
            } else {
                familyMemberHistory.setRelationship(convertToCodeableConcept(getCodeSystemEntriesService(), qdmDataElement.getRelationship()));
            }
        }

        FamilyMemberHistory.FamilyMemberHistoryConditionComponent  familyMemberHistoryConditionComponent =familyMemberHistory.getConditionFirstRep();
        familyMemberHistoryConditionComponent.setCode(convertToCodeSystems(getCodeSystemEntriesService(), qdmDataElement.getDataElementCodes()));

        return QdmToFhirConversionResult.<FamilyMemberHistory>builder()
                .fhirResource(familyMemberHistory)
                .conversionMessages(conversionMessages).build();
    }


}
