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

        return QdmToFhirConversionResult.<FamilyMemberHistory>builder()
                .fhirResource(familyMemberHistory)
                .conversionMessages(conversionMessages).build();
    }


}
