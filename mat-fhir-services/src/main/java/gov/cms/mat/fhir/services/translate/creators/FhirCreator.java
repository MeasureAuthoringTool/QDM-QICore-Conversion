package gov.cms.mat.fhir.services.translate.creators;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.Collections;

public interface FhirCreator {

    default CodeableConcept createType(String type, String code) {
        return new CodeableConcept()
                .setCoding(Collections.singletonList(new Coding(type, code, null)));
    }

    /* rawData are bytes that are NOT base64 encoded */
    default Attachment createAttachment(String contentType, byte[] rawData) {
        return new Attachment()
                .setContentType(contentType)
                .setData(rawData == null ? null : encodeBase64(rawData));
    }

    default byte[] encodeBase64(byte[] src) {
        return Base64.getEncoder().encode(src);
    }

    default String convertBytes(byte[] data) {
        if (data == null) {
            return null;
        } else {
            try {
                return IOUtils.toString(data, null);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    default Narrative createNarrative(String data) {
        Narrative narrative = new Narrative();
        narrative.setDivAsString(data);
        return narrative;
    }

    default Identifier createIdentifier(String system, String value) {
        return new Identifier()
                .setSystem(system)
                .setValue(value);
    }
}
