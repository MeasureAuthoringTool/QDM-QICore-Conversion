package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
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
    private final byte[] cql = null;
    private final byte[] elm = null;
    private CqlLibrary cqlLib;
    private final String baseURL = "http://localhost:8080/hapi-fhir-jpaserver/fhir/";
    private LibraryMapper libraryMapper;

    @BeforeEach
    void setUp() {
        cqlLib = new CqlLibrary();
        cqlLib.setMeasureId(MEASURE_ID);
        libraryMapper = new LibraryMapper(cqlLib, cql, elm, baseURL);
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
    void testTranslateToFhir_verifyAttachments() {


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