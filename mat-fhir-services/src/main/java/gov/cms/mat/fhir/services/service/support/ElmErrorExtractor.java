package gov.cms.mat.fhir.services.service.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.fhir.rest.dto.CqlConversionError;
import gov.cms.mat.fhir.rest.dto.MatCqlConversionException;
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

    public List<CqlConversionError> parseForAnnotations() {
        try {
            JsonNode annotationNode = getAnnotationNode();

            return processAnnotationNode(annotationNode);
        } catch (JsonProcessingException e) {
            log.warn("Error in parse", e);
            log.trace(json);
            throw new CqlConversionException("Error processing json: " + e.getMessage(), e);
        }
    }

    public List<MatCqlConversionException> parseForErrorExceptions() {
        try {
            JsonNode errorExceptionsNode = getErrorExceptionsNode();

            return processErrorExceptionNode(errorExceptionsNode);
        } catch (JsonProcessingException e) {
            log.warn("Error in parse", e);
            log.trace(json);
            throw new CqlConversionException("Error processing json", e);
        }
    }

    private List<MatCqlConversionException> processErrorExceptionNode(JsonNode errorExceptionsNode) {
        return processJsonNode(errorExceptionsNode, MatCqlConversionException.class);
    }

    public List<CqlConversionError> processAnnotationNode(JsonNode annotationNode) {
        return processJsonNode(annotationNode, CqlConversionError.class);
    }

    public <T> List<T> processJsonNode(JsonNode annotationNode, Class<T> type) {
        List<T> cqlConversionErrors = new ArrayList<>();

        if (annotationNode.isArray()) { // will not be array when isMissingNode
            for (JsonNode node : annotationNode) {
                cqlConversionErrors.add(mapper.convertValue(node, type));
            }
        }

        return cqlConversionErrors;
    }

    public JsonNode getAnnotationNode() throws JsonProcessingException {
        JsonNode libraryNode = getLibraryNode();

        JsonNode annotationNode = libraryNode.path("annotation");

        if (annotationNode.isMissingNode()) {
            log.trace("Json Does not contain a annotation Node"); // if no error this is normal
        }

        return annotationNode;
    }

    public JsonNode getLibraryNode() throws JsonProcessingException {
        JsonNode libraryNode = getNodeFromRoot("library");

        if (libraryNode.isMissingNode()) {
            log.trace(json);
            throw new CqlConversionException("Cannot find library node in json");
        }

        return libraryNode;
    }

    public JsonNode getErrorExceptionsNode() throws JsonProcessingException {
        JsonNode errorExceptionsNode = getNodeFromRoot("errorExceptions");

        if (errorExceptionsNode.isMissingNode()) {
            log.debug("Json Does not contain a errorExceptions Node"); // if no error this is normal
        }

        return errorExceptionsNode;
    }

    private JsonNode getNodeFromRoot(String fieldName) throws JsonProcessingException {
        JsonNode root = mapper.readTree(json);
        return root.path(fieldName);
    }
}
