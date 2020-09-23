package gov.cms.mat;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Library;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static junit.framework.TestCase.assertEquals;

@Slf4j
public class LibraryTest {
    @Test
    public void testLibEncoding() {
        String s = "TEST12345";
        log.info("s=" + s);
        Library library = new Library();
        library.setName("I am a library");
        library.setContent(new ArrayList<>());
        library.getContent().add(new Attachment()
                .setContentType("text/cql")
                .setData(s.getBytes()));
        FhirContext f = FhirContext.forR4();
        IParser parser = f.newJsonParser();
        String libJson = parser.encodeResourceToString(library);
        log.info("json: " + libJson);
        Library reversedLib = parser.parseResource(Library.class, libJson);
        byte[] decodedAttachment = reversedLib.getContent().get(0).getData();
        String decodedS = new String(decodedAttachment);
        log.info("Decoded s=" + decodedS);
        assertEquals(s, decodedS);
    }
}
