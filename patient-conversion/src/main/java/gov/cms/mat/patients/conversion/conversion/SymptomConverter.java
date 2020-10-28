package gov.cms.mat.patients.conversion.conversion;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.helpers.ObservationConverter;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class SymptomConverter extends ConverterBase<Observation> implements ObservationConverter {

    public static final String QDM_TYPE = "QDM::Symptom";

    public SymptomConverter(CodeSystemEntriesService codeSystemEntriesService,
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
    public QdmToFhirConversionResult<Observation> convertToFhir(Patient fhirPatient, QdmDataElement qdmDataElement) {
        //http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#822-symptom
        Observation observation = new Observation();
        List<String> conversionMessages = new ArrayList<>();

        observation.setId(qdmDataElement.get_id());
        observation.setSubject(createReference(fhirPatient));
        observation.setValue(convertToCodeSystems(getCodeSystemEntriesService(), qdmDataElement.getDataElementCodes()));
        //Todo qdmDataElement.getSeverity() is always null. Do we need to implement?
//        observation.setInterpretation(convertToCodeSystems(getCodeSystemEntriesService(), qdmDataElement.getSeverity());

        if (qdmDataElement.getRelevantPeriod() != null) {
            observation.setEffective(createFhirPeriod(qdmDataElement.getRelevantPeriod()));
        }

        return QdmToFhirConversionResult.<Observation>builder()
                .fhirResource(observation)
                .conversionMessages(conversionMessages)
                .build();
    }
}
