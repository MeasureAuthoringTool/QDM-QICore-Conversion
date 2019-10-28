package gov.cms.mat.fhir.services.translate;

import lombok.extern.slf4j.Slf4j;
import mat.client.measure.ManageCompositeMeasureDetailModel;
import mat.server.MeasureLibraryService;
import org.apache.commons.lang3.ArrayUtils;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Library;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class LibraryMapper implements FhirCreator {
    static final String SYSTEM_TYPE = "http://hl7.org/fhir/codesystem-library-type.html";
    static final String SYSTEM_CODE = "logic-library";
    static final String ELM_CONTENT_TYPE = "application/elm+xml";
    static final String CQL_CONTENT_TYPE = "text/cql";

    private final gov.cms.mat.fhir.commons.model.MeasureExport qdmMeasureExport;

    public LibraryMapper(gov.cms.mat.fhir.commons.model.MeasureExport qdmMeasureExport) {
        this.qdmMeasureExport = qdmMeasureExport;
    }

    public Library translateToFhir() {
        Library fhirLibrary = new Library();
        fhirLibrary.setId("Library/" + qdmMeasureExport.getMeasureId().getId());
        fhirLibrary.setDate(new Date());

        fhirLibrary.setType(createType(SYSTEM_TYPE, SYSTEM_CODE));

        fhirLibrary.setText(createNarrative(convertBytes(qdmMeasureExport.getHumanReadable())));
        fhirLibrary.setContent(createContent());

        log.debug("Converted library: {}", fhirLibrary);

        return fhirLibrary;
    }

    private List<Attachment> createContent() {
        List<Attachment> attachments = new ArrayList<>(2);

        attachments.add(createAttachment(ELM_CONTENT_TYPE, qdmMeasureExport.getElm()));
        attachments.add(createAttachment(CQL_CONTENT_TYPE, qdmMeasureExport.getCql()));

        return attachments;
    }

    private ManageCompositeMeasureDetailModel getFromXml(byte[] xmlBytes) {
        if (ArrayUtils.isNotEmpty(xmlBytes)) {
            try {
                return MeasureLibraryService.createModelFromXML(new String(xmlBytes));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            throw new IllegalArgumentException("Xml bytes are null");
        }
    }


}
