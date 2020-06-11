package gov.cms.mat.fhir.services.translate.creators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryResult;
import gov.cms.mat.fhir.services.components.fhir.FhirIncludeLibraryProcessor;
import gov.cms.mat.fhir.services.exceptions.CqlConversionException;
import gov.cms.mat.fhir.services.exceptions.cql.LibraryAttachmentNotFoundException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
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

    default FhirIncludeLibraryResult findIncludedLibraries(Library library,
                                                           FhirIncludeLibraryProcessor fhirIncludeLibraryProcessor) {
        if (CollectionUtils.isEmpty(library.getContent())) {
            throw new LibraryAttachmentNotFoundException(library);
        } else {
            Attachment cqlAttachment = findCqlAttachment(library, "text/cql");
            byte[] cqlBytes = Base64.decodeBase64(cqlAttachment.getData());
            return fhirIncludeLibraryProcessor.findIncludedFhirLibraries(new String(cqlBytes));
        }
    }
}
