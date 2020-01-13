package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.translate.creators.FhirCreator;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Library;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class LibraryTranslator implements FhirCreator {
    static final String SYSTEM_TYPE = "http://hl7.org/fhir/codesystem-library-type.html";
    static final String SYSTEM_CODE = "logic-library";
    static final String ELM_CONTENT_TYPE = "application/elm+xml";
    static final String CQL_CONTENT_TYPE = "text/cql";

    private final CqlLibrary cqlLibrary;
    private final byte[] cql;
    private final byte[] elm;
    private final String baseURL;

    public LibraryTranslator(CqlLibrary cqlLibrary, byte[] cql, byte[] elm, String baseURL) {
        this.cqlLibrary = cqlLibrary;
        this.cql = cql;
        this.elm = elm;
        this.baseURL = baseURL;
    }

    public Library translateToFhir() {
        Library fhirLibrary = new Library();
        fhirLibrary.setId(cqlLibrary.getId());
        fhirLibrary.setDate(new Date());
        fhirLibrary.setApprovalDate(cqlLibrary.getFinalizedDate());

        if (cqlLibrary.getVersion() != null) {
            fhirLibrary.setVersion(cqlLibrary.getVersion().toString());
        }

        fhirLibrary.setName(cqlLibrary.getCqlName());
        fhirLibrary.setUrl(baseURL + "Library/" + cqlLibrary.getId());

        fhirLibrary.setType(createType(SYSTEM_TYPE, SYSTEM_CODE));

        fhirLibrary.setContent(createContent());
        if (fhirLibrary.getContent().isEmpty()) {
            ConversionReporter.setLibraryFieldConversionResult("MAT.cql", "Library.content",
                    "No CQL or ELM to process", cqlLibrary.getId());
        }

        log.debug("Converted library: {}", fhirLibrary);

        return fhirLibrary;
    }

    private List<Attachment> createContent() {
        List<Attachment> attachments = new ArrayList<>(2);

        attachments.add(createAttachment(ELM_CONTENT_TYPE, elm));
        attachments.add(createAttachment(CQL_CONTENT_TYPE, cql));

        return attachments;
    }

}
