package gov.cms.mat.fhir.services.translate.creators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gov.cms.mat.fhir.services.exceptions.CqlConversionException;
import gov.cms.mat.fhir.services.exceptions.cql.LibraryAttachmentNotFoundException;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Library;

public interface FhirLibraryHelper {

    default Attachment findCqlAttachment(Library library, String type) {
        return library.getContent().stream()
                .filter(a -> a.getContentType().equals(type))
                .findFirst()
                .orElseThrow(() -> new LibraryAttachmentNotFoundException(library, type));
    }


    default String cleanJsonFromMatExceptions(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            ObjectNode root = (ObjectNode) mapper.readTree(json);
            root.remove("errorExceptions");
            root.remove("externalErrors");

            ObjectNode node = (ObjectNode) root.get("library");
            node.remove("annotation");

            return root.toPrettyString();
        } catch (JsonProcessingException e) {
            throw new CqlConversionException("Cannot clean json", e);
        }
    }
}
