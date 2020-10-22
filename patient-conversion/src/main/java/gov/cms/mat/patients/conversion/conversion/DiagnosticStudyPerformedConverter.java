package gov.cms.mat.patients.conversion.conversion;


import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
            log.info("(No type) Result: {}", qdmDataElement.getResult());
            processResult(observation, qdmDataElement.getResult());
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

    void processResult(Observation observation, JsonNode result) {

        if (result instanceof ObjectNode) {
            processObjectNode(observation, (ObjectNode) result);
        } else if (result instanceof NullNode) {
            log.debug("Null node");
        } else if (result instanceof TextNode) {
            processTextNode(observation, (TextNode) result);
        } else if (result instanceof IntNode) {
            log.debug("IntNode");
        } else if (result instanceof DoubleNode) {
            log.debug("DoubleNode");
        } else {
            log.warn(result.getClass().getName());
        }

    }

    private void processTextNode(Observation observation, TextNode result) {
       String data =  result.textValue();
       log.debug(data);

        LocalDateTime dateTime = LocalDateTime.parse("2018-05-05T11:50:55");




    }

    void processObjectNode(Observation observation, ObjectNode objectNode) {

        JsonNode codeNode = objectNode.get("code");
        JsonNode systemNode = objectNode.get("system"); // oid

        if (codeNode != null && systemNode != null) {
            QdmCodeSystem qdmCodeSystem = new QdmCodeSystem();
            qdmCodeSystem.setCode(codeNode.asText());
            qdmCodeSystem.setSystem(systemNode.asText());

            JsonNode displayNode = objectNode.get("display");

            if (displayNode != null) {
                qdmCodeSystem.setDisplay(displayNode.asText());
            }

            observation.setValue(convertToCodeableConcept(codeSystemEntriesService, qdmCodeSystem));
        }


        JsonNode unitNode = objectNode.get("unit");
        JsonNode valueNode = objectNode.get("value"); // oid

        if (unitNode != null && valueNode != null) {
            Quantity quantity = new Quantity();
            quantity.setValue(valueNode.asInt());
            quantity.setSystem("http://unitsofmeasure.org");
            quantity.setCode(convertUnitToCode(unitNode.asText()));
            observation.setValue(quantity);
        }

    }


}
