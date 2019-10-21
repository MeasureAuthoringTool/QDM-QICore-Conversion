package gov.cms.mat.fhir.services.translate;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LibraryMapper {
    static final String SYSTEM_TYPE = "http://hl7.org/fhir/codesystem-library-type.html";
    static final String SYSTEM_CODE = "logic-library";
    static final String ELM_CONTENT_TYPE = "application/elm+xml";
    static final String CQL_CONTENT_TYPE = "text/cql";

    private static final Logger LOGGER = Logger.getLogger(LibraryMapper.class.getName());
    private final gov.cms.mat.fhir.commons.model.MeasureExport qdmMeasureExport;

    public LibraryMapper(gov.cms.mat.fhir.commons.model.MeasureExport qdmMeasureExport) {
        this.qdmMeasureExport = qdmMeasureExport;
    }

    public Library translateToFhir() {
        /* todo MCG  from XML
        MeasureExport measureExport = getMeasureExport(measureId);
		String simpleXML = measureExport.getSimpleXML();
		CQLModel cqlModel = CQLUtilityClass.getCQLModelFromXML(simpleXML);
         */
        Library fhirLibrary = new Library();
        fhirLibrary.setId("Library/" + qdmMeasureExport.getMeasureId().getId());
        fhirLibrary.setDate(new Date());

        fhirLibrary.setType(createType());

        fhirLibrary.setText(createNarrative());
        fhirLibrary.setContent(createContent());

        LOGGER.log(Level.FINER, "Converted library: {}", fhirLibrary);

        return fhirLibrary;
    }

    private CodeableConcept createType() {
        return new CodeableConcept()
                .setCoding(Collections.singletonList(new Coding(SYSTEM_TYPE, SYSTEM_CODE, null)));
    }

    private Narrative createNarrative() {
        Narrative narrative = new Narrative();
        narrative.setDivAsString(convertBytes(qdmMeasureExport.getHumanReadable()));
        return narrative;
    }

    private List<Attachment> createContent() {
        List<Attachment> attachments = new ArrayList<>(2);

        attachments.add(createAttachment(ELM_CONTENT_TYPE, qdmMeasureExport.getElm()));
        attachments.add(createAttachment(CQL_CONTENT_TYPE, qdmMeasureExport.getCql()));

        return attachments;
    }

    /* rawData are bytes that are NOT base64 encoded */
    private Attachment createAttachment(String contentType, byte[] rawData) {
        return new Attachment()
                .setContentType(contentType)
                .setData(rawData == null ? null : encodeBase64(rawData));
    }

    private byte[] encodeBase64(byte[] src) {
        return Base64.getEncoder().encode(src);
    }

    private String convertBytes(byte[] data) {
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
}
