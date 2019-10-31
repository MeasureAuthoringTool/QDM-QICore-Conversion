package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.commons.model.MeasureExport;
import org.hl7.fhir.r4.model.Library;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.Date;

import static gov.cms.mat.fhir.services.translate.LibraryMapper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


class LibraryMapperTest {
    private final String MEASURE_ID = "ID";
    private MeasureExport measureExport;
    private LibraryMapper libraryMapper;

    @BeforeEach
    void setUp() {
        measureExport = new MeasureExport();
        //measureExport.setMeasureId(new Measure());
        measureExport.setMeasureId(MEASURE_ID);
        libraryMapper = new LibraryMapper(measureExport);
    }

    @Test
    void testTranslateToFhir_SendEmptyMeasureExport() {
        Library library = libraryMapper.translateToFhir();

        assertEquals(library.getDate().getTime(), new Date().getTime(), 10L);
        assertEquals("Library/" + MEASURE_ID, library.getId());

        assertEquals(1, library.getType().getCoding().size());
        assertEquals(SYSTEM_TYPE, library.getType().getCoding().get(0).getSystem());
        assertEquals(SYSTEM_CODE, library.getType().getCoding().get(0).getCode());
    }

    @Test
    void testTranslateToFhir_verifyNarrative() {
        final String readable = "Readable";
        measureExport.setHumanReadable(readable.getBytes());
        Library library = libraryMapper.translateToFhir();

        assertEquals(readable, library.getText().getDiv().getChildNodes().get(0).getContent());
    }

    @Test
    void testTranslateToFhir_verifyAttachments() {
        final String cql = "CQL";
        final String elm = "ELM";

        measureExport.setCql(cql.getBytes());
        measureExport.setElm(elm.getBytes());

        Library library = libraryMapper.translateToFhir();

        assertEquals(2, library.getContent().size());

        assertEquals(elm, decodeBase64(library.getContent().get(0).getData()));
        assertEquals(ELM_CONTENT_TYPE, library.getContent().get(0).getContentType());

        assertEquals(cql, decodeBase64(library.getContent().get(1).getData()));
        assertEquals(CQL_CONTENT_TYPE, library.getContent().get(1).getContentType());
    }

    private String decodeBase64(byte[] bytes) {
        return new String(Base64.getDecoder().decode(bytes));
    }
}