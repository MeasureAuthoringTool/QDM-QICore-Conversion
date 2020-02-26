package gov.cms.mat.fhir.services.translate.creators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gov.cms.mat.cql.elements.BaseProperties;
import gov.cms.mat.fhir.services.components.library.UnConvertedCqlLibraryHandler;
import gov.cms.mat.fhir.services.exceptions.CqlConversionException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Library;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

public interface FhirLibraryHelper {

    default String findVersion(String orderedName, String defaultVersion) {
        String startPattern = BaseProperties.LIBRARY_FHIR_EXTENSION + "-";
        String version = StringUtils.substringBetween(orderedName, startPattern, UnConvertedCqlLibraryHandler.EXTENSION);

        return StringUtils.isEmpty(version) ? defaultVersion : version;
    }

    default Optional<String> findLibFile(List<String> libFileNames, String fhir4Name) {
        return libFileNames.stream()
                .filter(s -> nameMatch(s, fhir4Name))
                .findFirst();
    }

    private boolean nameMatch(String orderedName, String fhir4Name) {
        int end = orderedName.indexOf(BaseProperties.LIBRARY_FHIR_EXTENSION);

        if (end == -1) {
            return false;
        }

        String name = orderedName.substring(0, end + BaseProperties.LIBRARY_FHIR_EXTENSION.length());

        return name.equals(fhir4Name);
    }

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
        try {
            ObjectMapper mapper = new ObjectMapper();

            ObjectNode root = (ObjectNode) mapper.readTree(json);
            root.remove("errorExceptions");

            ObjectNode node = (ObjectNode) root.get("library");
            node.remove("annotation");

            return root.toPrettyString();
        } catch (JsonProcessingException e) {
            throw new CqlConversionException("Cannot clean json", e);
        }
    }
}
