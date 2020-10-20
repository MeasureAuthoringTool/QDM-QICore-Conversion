package gov.cms.mat.patients.conversion.conversion;


import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Goal;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class CareCoalConverter extends ConverterBase<Goal> {
    public static final String QDM_TYPE = "QDM::CareGoal";

    public CareCoalConverter(CodeSystemEntriesService codeSystemEntriesService,
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
        Goal goal = new Goal();

//       if(  qdmDataElement.getRelevantPeriod() != null ) {
//           goal.setStart(new DateTimeType())
//
//       }
//        goal.setStart()
//
//        goal.setTarget()
//
//
//
//
//        qdmDataElement.getTargetOutcome();


        processNegation(qdmDataElement, goal);

        return QdmToFhirConversionResult.builder()
                .fhirResource(goal)
                .conversionMessages(conversionMessages)
                .build();
    }


}
