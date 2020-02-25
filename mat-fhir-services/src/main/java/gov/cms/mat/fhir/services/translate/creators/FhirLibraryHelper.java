package gov.cms.mat.fhir.services.translate.creators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Library;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

public interface FhirLibraryHelper {
    default String findContentFromLibrary(Library library, String type) {
        Optional<Attachment> optionalAttachment = findAttachment(library.getContent(), type);

        return optionalAttachment
                .map(attachment -> decodeBase64(attachment.getData()))
                .orElse(null);
    }

    private Optional<Attachment> findAttachment(List<Attachment> attachments, String type) {
        if (CollectionUtils.isEmpty(attachments)) {
            return Optional.empty();
        } else {
            return attachments.stream()
                    .filter(attachment -> attachment.getContentType().equals(type))
                    .findFirst();
        }
    }

    default String decodeBase64(byte[] src) {
        return new String(Base64.getDecoder().decode(src));
    }

    default String cleanJsonFromMatExceptions(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            ObjectNode root = (ObjectNode) mapper.readTree(json);
            root.remove("errorExceptions");

            ObjectNode node = (ObjectNode) root.get("library");
            node.remove("annotation");

            return root.toPrettyString();

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return json;
        }
    }
}
