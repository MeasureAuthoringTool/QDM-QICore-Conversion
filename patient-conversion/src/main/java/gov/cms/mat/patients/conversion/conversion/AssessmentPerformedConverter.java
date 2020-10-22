package gov.cms.mat.patients.conversion.conversion;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class AssessmentPerformedConverter extends ConverterBase<Observation> {
    public static final String QDM_TYPE = "QDM::AssessmentPerformed";



    public AssessmentPerformedConverter(CodeSystemEntriesService codeSystemEntriesService,
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
        Observation observation = new Observation();
        observation.setId(qdmDataElement.get_id());
        observation.setSubject(createReference(fhirPatient));

        //serviceRequest.setAuthoredOn(qdmDataElement.getAuthorDatetime());

        observation.setCode(convertToCodeSystems(codeSystemEntriesService, qdmDataElement.getDataElementCodes()));


        if (qdmDataElement.getResult() != null) {
            //todo how to map this an take values like below, types be damned full speed ahead

            // Result: {"unit":"weeks","value":3 7}
            //  "2012-01-01T11:00:00.000+00:00"
            // the string null
            // ints less than 100
            //{"code":"110483000","version":null,"system":"2.16.840.1.113883.6.96","display":"Tobacco User"}
            log.info("(No type) Result: {}", qdmDataElement.getResult());
        }

        if (!processNegation(qdmDataElement, observation)) {
            //http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#842-assessment-performed
            //Constrain status to -  final, amended, corrected
            observation.setStatus(Observation.ObservationStatus.UNKNOWN);
            conversionMessages.add(NO_STATUS_MAPPING);
        }

        return QdmToFhirConversionResult.builder()
                .fhirResource(observation)
                .conversionMessages(conversionMessages)
                .build();
    }

    @Override
    void convertNegation(QdmDataElement qdmDataElement, Observation observation) {
        convertNegationObservation(qdmDataElement, observation);
    }
}
