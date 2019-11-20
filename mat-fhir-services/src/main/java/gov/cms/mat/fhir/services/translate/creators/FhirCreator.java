package gov.cms.mat.fhir.services.translate.creators;

import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

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


    default Identifier createIdentifier(String system, String value) {
        return new Identifier()
                .setSystem(system)
                .setValue(value);
    }

    default CodeableConcept buildCodeableConcept(String code, String system, String display) {
        CodeableConcept cp = new CodeableConcept();
        List<Coding> lC = new ArrayList<>();
        Coding cd = new Coding();
        cd.setCode(code);
        cd.setSystem(system);
        cd.setDisplay(display);
        lC.add(cd);
        cp.setCoding(lC);

        return cp;
    }
}
