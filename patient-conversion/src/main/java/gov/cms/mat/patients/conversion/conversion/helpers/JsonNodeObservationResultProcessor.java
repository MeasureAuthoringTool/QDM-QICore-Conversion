package gov.cms.mat.patients.conversion.conversion.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import gov.cms.mat.patients.conversion.dao.QdmCodeSystem;
import gov.cms.mat.patients.conversion.exceptions.PatientConversionException;
import gov.cms.mat.patients.conversion.service.CodeSystemEntriesService;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.StringType;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Slf4j
public class JsonNodeObservationResultProcessor implements FhirCreator, DataElementFinder {
    private final CodeSystemEntriesService codeSystemEntriesService;
    private final Observation observation;
    private final List<String> conversionMessages;

    public JsonNodeObservationResultProcessor(Observation observation,
                                              CodeSystemEntriesService codeSystemEntriesService,
                                              List<String> conversionMessages) {
        this.observation = observation;
        this.codeSystemEntriesService = codeSystemEntriesService;
        this.conversionMessages = conversionMessages;
    }

    public void processNode(JsonNode result) {
        if (result == null) {
            log.debug("JsonNode is null");
            return;
        }

        if (result instanceof ObjectNode) {
            processObjectNode((ObjectNode) result);
        } else if (result instanceof NullNode) {
            log.trace("Null node ignored");
        } else if (result instanceof TextNode) {
            processTextNode(observation, (TextNode) result);
        } else if (result instanceof IntNode) {
            processIntMode((IntNode) result);
        } else if (result instanceof DoubleNode) {
            conversionMessages.add("Observation result does not handle doubles value: " + result.asText());
        } else {
            log.warn(result.getClass().getName());
        }
    }

    private void processIntMode(IntNode result) {
        observation.setValue(new IntegerType(result.intValue()));
    }

    void processObjectNode(ObjectNode objectNode) {
        if (!processValueCodeableConcept(objectNode)) {
            processValueQuantity(objectNode);
        }
    }

    private void processValueQuantity(ObjectNode objectNode) {
        JsonNode unitNode = objectNode.get("unit");
        JsonNode valueNode = objectNode.get("value"); // oid

        if (unitNode != null && valueNode != null) {
            Quantity quantity = new Quantity();
            quantity.setValue(valueNode.asInt());
            quantity.setSystem("http://unitsofmeasure.org");

            try {
                quantity.setCode(convertUnitToCode(unitNode.asText()));
            } catch (PatientConversionException e) {
                conversionMessages.add(e.getMessage());
            }

            observation.setValue(quantity);
        }
    }

    private boolean processValueCodeableConcept(ObjectNode objectNode) {
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
            return true;
        } else {
            return false;
        }
    }

    private void processTextNode(Observation observation, TextNode result) {
        String data = result.textValue();

        try {
            LocalDateTime dateTime = LocalDateTime.parse(data, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            Date date = convertToDateViaInstant(dateTime);

            observation.setValue(new DateTimeType(date));
        } catch (Exception e) { // Not a date
            observation.setValue(new StringType(data));
        }
    }

    Date convertToDateViaInstant(LocalDateTime dateToConvert) {
        return java.util.Date
                .from(dateToConvert.atZone(ZoneId.systemDefault())
                        .toInstant());
    }
}
