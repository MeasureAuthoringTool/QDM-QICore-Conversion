package gov.cms.mat.fhir.services.service.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.fhir.services.exceptions.CqlConversionException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ElmErrorExtractor {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final String json;

    public ElmErrorExtractor(String json) {
        this.json = json;
    }

    public List<CqlConversionError> parse() {
        try {
            JsonNode annotationNode = getAnnotationNode();

            return processAnnotationNode(annotationNode);
        } catch (JsonProcessingException e) {
            log.trace(json);
            throw new CqlConversionException("Error processing json", e);
        }
    }

    public List<CqlConversionError> processAnnotationNode(JsonNode annotationNode) {
        List<CqlConversionError> cqlConversionErrors = new ArrayList<>();

        if (annotationNode.isArray()) { // will not be array when isMissingNode
            for (JsonNode node : annotationNode) {
                cqlConversionErrors.add(mapper.convertValue(node, CqlConversionError.class));
            }
        }

        return cqlConversionErrors;
    }

    public JsonNode getAnnotationNode() throws JsonProcessingException {
        JsonNode libraryNode = getLibraryNode();

        JsonNode annotationNode = libraryNode.path("annotation");

        if (annotationNode.isMissingNode()) {
            log.trace("Does not contain a Annotation Node"); // if no error this is normal
        }

        return annotationNode;
    }

    public JsonNode getLibraryNode() throws JsonProcessingException {
        JsonNode root = mapper.readTree(json);
        JsonNode libraryNode = root.path("library");

        if (libraryNode.isMissingNode()) {
            log.trace(json);
            throw new CqlConversionException("Cannot find library node in json");
        }

        return libraryNode;
    }
}
