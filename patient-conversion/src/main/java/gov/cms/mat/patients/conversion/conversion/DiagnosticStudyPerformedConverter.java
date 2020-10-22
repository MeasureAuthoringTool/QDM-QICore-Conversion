package gov.cms.mat.patients.conversion.conversion;


import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import gov.cms.mat.patients.conversion.conversion.helpers.JsonNodeObservationResultProcessor;
import gov.cms.mat.patients.conversion.conversion.results.QdmToFhirConversionResult;
import gov.cms.mat.patients.conversion.dao.QdmCodeSystem;
import gov.cms.mat.patients.conversion.dao.QdmDataElement;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import gov.cms.mat.patients.conversion.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Quantity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class DiagnosticStudyPerformedConverter extends ConverterBase<Observation> {
    public static final String QDM_TYPE = "QDM::DiagnosticStudyPerformed";

    public DiagnosticStudyPerformedConverter(CodeSystemEntriesService codeSystemEntriesService,
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
        observation.setCode(convertToCodeSystems(codeSystemEntriesService, qdmDataElement.getDataElementCodes()));
        observation.setEffective(createFhirPeriod(qdmDataElement.getRelevantPeriod()));

        if (qdmDataElement.getResult() != null) {
            JsonNodeObservationResultProcessor resultProcessor =
                    new JsonNodeObservationResultProcessor(observation, codeSystemEntriesService, conversionMessages);

            resultProcessor.processNode(qdmDataElement.getResult());
        }

        if (!processNegation(qdmDataElement, observation)) {
            //http://hl7.org/fhir/us/qicore/qdm-to-qicore.html#8102-diagnostic-study-performed
            //Constrain status to -  final, amended, corrected, appended
            observation.setStatus(Observation.ObservationStatus.UNKNOWN);
            conversionMessages.add(NO_STATUS_MAPPING);
        }

        qdmDataElement.getRelevantDatetime();

        observation.setIssued(qdmDataElement.getAuthorDatetime());


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
