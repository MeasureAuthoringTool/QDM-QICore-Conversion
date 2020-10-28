package gov.cms.mat.cql_elm_translation.service.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gov.cms.mat.cql.elements.LibraryProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Slf4j
public class AnnotationErrorFilter implements CqlLibraryFinder, JsonHelpers {
    @Getter
    private final String cqlData;
    private final boolean showWarnings;
    private final String json;

    private LibraryProperties libraryProperties;

    private List<JsonNode> keeperList = new ArrayList<>();

    private List<JsonNode> externalList = new ArrayList<>();
    private ObjectMapper objectMapper = new ObjectMapper();

    public AnnotationErrorFilter(String cqlData, boolean showWarnings, String json) {
        this.cqlData = cqlData;
        this.showWarnings = showWarnings;
        this.json = json;
    }

    public String filter() {
        try {
            JsonNode rootNode = readRootNode();

            Optional<ArrayNode> optionalArrayNode = getAnnotationNode(rootNode);

            if (optionalArrayNode.isEmpty()) {
                return json;
            } else {
                libraryProperties = parseLibrary();
                return processArrayNode(rootNode, optionalArrayNode.get());
            }
        } catch (Exception e) {
            log.info("Error filtering annotations", e);
            return json;
        }
    }

    private JsonNode readRootNode() throws JsonProcessingException {
        return objectMapper.readTree(json);
    }

    private String processArrayNode(JsonNode rootNode, ArrayNode annotationArrayNode) {
        annotationArrayNode.forEach(this::filterByNode);

        annotationArrayNode.removeAll();
        annotationArrayNode.addAll(keeperList);

        if (rootNode instanceof ObjectNode) {
            ObjectNode rootObjectNode = (ObjectNode) rootNode;

            ArrayNode arrayNode = objectMapper.createArrayNode();
            arrayNode.addAll(externalList);

            rootObjectNode.set("externalErrors", arrayNode);
        }

        String cleanedJson = rootNode.toPrettyString();
        return fixErrorTags(cleanedJson);
    }

    private void filterByNode(JsonNode jsonNode) {
        if (!isLibraryNodeValid(jsonNode)) {
            return;
        }

        if (!showWarnings && !isError(jsonNode)) {
            return;
        }

        if (filterNodeByLibrary(jsonNode)) {
            keeperList.add(jsonNode);
        } else {
            externalList.add(jsonNode);
        }
    }

    private boolean isTargetLibrary(JsonNode jsonNode) {
        return filterNodeByLibrary(jsonNode);
    }

    private String fixErrorTags(String cleanedJson) {
        return cleanedJson.replace("\"errorSeverity\" : \"error\"",
                "\"errorSeverity\" : \"Error\"");
    }

    private Optional<ArrayNode> getAnnotationNode(JsonNode rootNode) {
        JsonNode libraryNode = rootNode.get("library");
        JsonNode annotationNode = libraryNode.get("annotation");

        return validateArrayNode(annotationNode);
    }

    private Optional<ArrayNode> validateArrayNode(JsonNode annotationNode) {
        if (annotationNode == null || annotationNode.isMissingNode()) {
            log.info("Annotation node is missing");
            return Optional.empty();
        }

        if (annotationNode.size() == 0) {
            log.info("Annotation node is empty");
            return Optional.empty();
        }

        if (annotationNode instanceof ArrayNode) {
            return Optional.of((ArrayNode) annotationNode);
        } else {
            log.info("Annotation node is not a ArrayNode, class: {}", annotationNode.getClass().getSimpleName());
            return Optional.empty();
        }
    }


    private boolean filterNodeByLibrary(JsonNode node) {
        Optional<String> optionalLibraryId = getLibraryId(node);
        Optional<String> optionalLibraryVersion = getLibraryVersion(node);

        if (optionalLibraryId.isEmpty() || optionalLibraryVersion.isEmpty()) {
            return false;
        } else {
            return isPointingToSameLibrary(libraryProperties,
                    optionalLibraryId.get(),
                    optionalLibraryVersion.get());
        }
    }

    private Optional<String> getLibraryId(JsonNode node) {
        return getTextFromNodeId(node, "libraryId");
    }

    private Optional<String> getLibraryVersion(JsonNode node) {
        return getTextFromNodeId(node, "libraryVersion");
    }

    private boolean isLibraryNodeValid(JsonNode node) {
        Optional<String> optionalLibraryId = getLibraryId(node);
        Optional<String> optionalLibraryVersion = getLibraryVersion(node);

        return optionalLibraryId.isPresent() && optionalLibraryVersion.isPresent();
    }


    private boolean isPointingToSameLibrary(LibraryProperties p,
                                            String libraryId,
                                            String version) {
        return p.getName().equals(libraryId) && p.getVersion().equals(version);
    }

    private boolean isError(JsonNode node) {
        JsonNode errorSeverityNode = node.path("errorSeverity");

        var optional = getSeverityValueFromNode(errorSeverityNode);

        return optional.isPresent() && optional.get().equals("error");
    }

    private Optional<String> getSeverityValueFromNode(JsonNode errorSeverityNode) {
        var optional = getTextFromNode(errorSeverityNode);
        return optional.map(s -> s.toLowerCase().trim());
    }


}
