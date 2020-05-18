package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.services.translate.creators.FhirCreator;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Library;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public abstract class LibraryTranslatorBase implements FhirCreator {
    public static final String CQL_CONTENT_TYPE = "text/cql";
    public static final String JSON_ELM_CONTENT_TYPE = "application/elm+json";
    public static final String XML_ELM_CONTENT_TYPE = "application/elm+xml";
    static final String SYSTEM_TYPE = "http://hl7.org/fhir/codesystem-library-type.html";
    static final String SYSTEM_CODE = "logic-library";
    final byte[] cql;
    final byte[] elmJson;
    final byte[] elmXml;

    final String baseURL;

    public LibraryTranslatorBase(byte[] cql, byte[] elmJson, byte[] elmXml, String baseURL) {
        this.cql = cql;
        this.elmJson = elmJson;
        this.elmXml = elmXml;
        this.baseURL = baseURL;
    }

    public Library translateToFhir(@Nullable String version) {
        Library fhirLibrary = buildBoilerplateLibrary();

        translate(version, fhirLibrary);

        log.debug("Converted library: {}", fhirLibrary);

        return fhirLibrary;
    }

    public Library buildBoilerplateLibrary() {
        return new Library()
                .setDate(new Date())
                .setStatus(Enumerations.PublicationStatus.ACTIVE) // assume this is active
                .setContent(createContent())
                .setType(createType(SYSTEM_TYPE, SYSTEM_CODE));
    }

    abstract void translate(@Nullable String version, Library fhirLibrary);

    List<Attachment> createContent() {
        List<Attachment> attachments = new ArrayList<>(2);

        attachments.add(createAttachment(JSON_ELM_CONTENT_TYPE, elmJson));
        attachments.add(createAttachment(CQL_CONTENT_TYPE, cql));
        attachments.add(createAttachment(XML_ELM_CONTENT_TYPE, elmXml));

        return attachments;
    }

    String createVersion(CqlLibrary cqlLibrary) {
        return createVersion(cqlLibrary.getVersion(), cqlLibrary.getRevisionNumber());
    }
}
