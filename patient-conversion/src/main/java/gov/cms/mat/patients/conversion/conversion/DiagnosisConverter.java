package gov.cms.mat.patients.conversion.conversion;


import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class DiagnosisConverter extends ConverterBase<Condition> {
    public static final String QDM_TYPE = "QDM::Diagnosis";

    public DiagnosisConverter(CodeSystemEntriesService codeSystemEntriesService,
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

        Condition condition = new Condition();

        condition.setCode(convertToCodeSystems(codeSystemEntriesService, qdmDataElement.getDataElementCodes()));

        condition.setRecordedDate(qdmDataElement.getAuthorDatetime()); // usually comes in as null

        condition.setOnset(createFhirPeriod(qdmDataElement.getPrevalencePeriod()));

        if(CollectionUtils.isNotEmpty(qdmDataElement.getFacilityLocations())) {
            log.debug("We have FacilityLocations");
        }

        processNegation(qdmDataElement, condition);

        return QdmToFhirConversionResult.builder()
                .fhirResource(condition)
                .conversionMessages(conversionMessages)
                .build();
    }


}
