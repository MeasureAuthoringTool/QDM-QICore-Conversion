package gov.cms.mat.fhir.services;

import ca.uhn.fhir.context.FhirContext;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.junit.Test;

import java.util.ArrayList;

@Slf4j
public class ExtensionTest {

    @Test
    public void testExtension() {
        Measure m = new Measure();
        Extension e = new Extension("http://example.com#childOne", new CodeType("boolean"));
        Extension e2 = new Extension("http://example.com#childOne", new Reference("#cqf-tooling"));
        m.setExtension(new ArrayList<>());
        m.getExtension().add(e);
        m.getExtension().add(e2);
        FhirContext c = FhirContext.forR4();
        log.info(c.newJsonParser().encodeResourceToString(m));
    }
}
