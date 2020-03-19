package gov.cms.mat.fhir.services.translate;

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
    static final String SYSTEM_TYPE = "http://hl7.org/fhir/codesystem-library-type.html";
    static final String SYSTEM_CODE = "logic-library";
    public static final String ELM_CONTENT_TYPE = "application/elm+json";

    final byte[] cql;
    final byte[] elm;
    final String baseURL;

    public LibraryTranslatorBase(byte[] cql, byte[] elm, String baseURL) {
        this.cql = cql;
        this.elm = elm;
        this.baseURL = baseURL;
    }

    public Library translateToFhir(@Nullable String version) {
        Library fhirLibrary = new Library()
                .setDate(new Date())
                .setStatus(Enumerations.PublicationStatus.ACTIVE) // assume this is active
                .setContent(createContent())
                .setType(createType(SYSTEM_TYPE, SYSTEM_CODE));

        translate(version, fhirLibrary);

        log.debug("Converted library: {}", fhirLibrary);

        return fhirLibrary;
    }

    abstract void translate(@Nullable String version, Library fhirLibrary);

    List<Attachment> createContent() {
        List<Attachment> attachments = new ArrayList<>(2);

        attachments.add(createAttachment(ELM_CONTENT_TYPE, elm));
        attachments.add(createAttachment(CQL_CONTENT_TYPE, cql));

        return attachments;
    }
}
