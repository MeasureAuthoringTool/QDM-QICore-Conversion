package gov.cms.mat.cql_elm_translation.service.filters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import gov.cms.mat.cql.elements.LibraryProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class AnnotationErrorFilter implements CqlLibraryFinder {

    @Getter
    private final String cqlData;
    private final boolean showWarnings;
    private final String json;
    private ObjectMapper objectMapper = new ObjectMapper();

    public AnnotationErrorFilter(String cqlData, boolean showWarnings, String json) {
        this.cqlData = cqlData;
        this.showWarnings = showWarnings;
        this.json = json;
    }

    public String filter() {
        try {
            JsonNode rootNode = objectMapper.readTree(json);

            var optional = getAnnotationNode(rootNode);

            if (optional.isEmpty()) {
                return json;
            }

            ArrayNode annotationArrayNode = optional.get();

            List<Integer> deleteArray = new ArrayList<>();

            if (showWarnings) {
                deleteArray.addAll(filterOutWarnings(annotationArrayNode));
            }

            if (annotationArrayNode.size() > 0) {
                deleteArray.addAll(filterByLibrary(annotationArrayNode));
            }

            deleteArray.forEach(annotationArrayNode::remove);

            String cleanedJson = rootNode.toPrettyString();
            return fixErrorTags(cleanedJson);
        } catch (Exception e) {
            return json;
        }
    }

    private String fixErrorTags(String cleanedJson) {
        return cleanedJson.replace("\"errorSeverity\" : \"error\"", "\"errorSeverity\" : \"Error\"");
    }

    private Optional<ArrayNode> getAnnotationNode(JsonNode rootNode) {
        JsonNode libraryNode = rootNode.get("library");
        JsonNode annotationNode = libraryNode.get("annotation");

        if (annotationNode.isMissingNode()) {
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


    private List<Integer> filterByLibrary(ArrayNode annotationArrayNode) {
        List<Integer> deleteIndexes = new ArrayList<>();
        LibraryProperties libraryProperties = parseLibrary();

        int index = 0;

        for (JsonNode node : annotationArrayNode) {
            JsonNode libraryIdNode = node.path("libraryId");
            var optionalLibraryId = getTextFromNode(libraryIdNode);

            JsonNode libraryVersionNode = node.path("libraryVersion");
            var optionalLibraryVersion = getTextFromNode(libraryVersionNode);

            if (optionalLibraryId.isEmpty() || optionalLibraryVersion.isEmpty()) {
                log.debug("Library and/or version is missing");
                deleteIndexes.add(index);
            } else {
                String libraryId = optionalLibraryId.get();
                String libraryVersion = optionalLibraryVersion.get();

                if (!isPointingToSameLibrary(libraryProperties, libraryId, libraryVersion)) {
                    deleteIndexes.add(index);
                }
            }

            index++;
        }

        return deleteIndexes;
    }

    private boolean isPointingToSameLibrary(LibraryProperties p, String libraryId, String version) {
        return p.getName().equals(libraryId) && p.getVersion().equals(version);
    }


    private List<Integer> filterOutWarnings(ArrayNode annotationArrayNode) {
        List<Integer> deleteIndexes = new ArrayList<>();
        int index = 0;

        for (JsonNode node : annotationArrayNode) {
            JsonNode errorSeverityNode = node.path("errorSeverity");

            var optional = checkErrorSeverityValue(errorSeverityNode);

            if (optional.isPresent() && optional.get().equals("error")) {
                index++;
                continue;
            }

            deleteIndexes.add(index);

            index++;
        }

        return deleteIndexes;
    }

    private Optional<String> checkErrorSeverityValue(JsonNode errorSeverityNode) {
        var optional = getTextFromNode(errorSeverityNode);
        return optional.map(s -> s.toLowerCase().trim());
    }

    private Optional<String> getTextFromNode(JsonNode jsonNode) {
        if (jsonNode.isMissingNode()) {
            log.info("jsonNode is missing");
            return Optional.empty();
        }

        String textValue = jsonNode.toString();

        if (StringUtils.isEmpty(textValue)) {
            log.info("jsonNode is empty");
            return Optional.empty();
        }

        textValue = StringUtils.replace(textValue, "\"", "");

        return Optional.of(textValue);
    }
}
