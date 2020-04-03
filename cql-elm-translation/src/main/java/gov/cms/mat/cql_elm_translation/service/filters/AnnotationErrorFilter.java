package gov.cms.mat.cql_elm_translation.service.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(json);
    }

    private String processArrayNode(JsonNode rootNode, ArrayNode annotationArrayNode) {
        annotationArrayNode.forEach(this::filterByNode);

        annotationArrayNode.removeAll();
        annotationArrayNode.addAll(keeperList);

        String cleanedJson = rootNode.toPrettyString();
        return fixErrorTags(cleanedJson);
    }

    private void filterByNode(JsonNode jsonNode) {
        if (filterNodeByLibrary(jsonNode)) {
            if (showWarnings) {
                keeperList.add(jsonNode);
            } else if (isError(jsonNode)) {
                keeperList.add(jsonNode);
            }
        }
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
        Optional<String> optionalLibraryId = getTextFromNodeId(node, "libraryId");
        Optional<String> optionalLibraryVersion = getTextFromNodeId(node, "libraryVersion");

        if (optionalLibraryId.isEmpty() || optionalLibraryVersion.isEmpty()) {
            log.debug("Library and/or version is missing");
            return false;
        } else {
            return isPointingToSameLibrary(libraryProperties,
                    optionalLibraryId.get(),
                    optionalLibraryVersion.get());
        }
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
