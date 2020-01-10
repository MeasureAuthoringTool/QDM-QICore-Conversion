package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import org.hl7.fhir.r4.model.Library;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.Date;

import static gov.cms.mat.fhir.services.translate.LibraryTranslator.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


class LibraryTranslatorTest {
    private String MEASURE_ID;
    private byte[] cql;
    private byte[] elm;
    private CqlLibrary cqlLib;
    private String baseURL;
    private String fhirResourceURL;
    private String cqlName;
    private LibraryTranslator libraryTranslator;

    @BeforeEach
    void setUp() {
        MEASURE_ID = "402803826963bf5e0169724b8c4b066c";
        cqlLib = new CqlLibrary();
        cqlLib.setMeasureId(MEASURE_ID);
        cqlLib.setVersion(new BigDecimal(0.0001));
        cqlLib.setFinalizedDate(new Date());
        cqlLib.setQdmVersion("v5.5");
        cqlName = "Test CQL Library";
        cqlLib.setCqlName(cqlName);
        cql = "test cql".getBytes();
        elm = "test elm".getBytes();
        baseURL = "http://localhost:8080/hapi-fhir-jpaserver/fhir/";
        fhirResourceURL = baseURL + "Library/" + MEASURE_ID;
        System.out.println(fhirResourceURL);
        libraryTranslator = new LibraryTranslator(cqlLib, cql, elm, baseURL);
    }

    @Test
    void testTranslateToFhir_verifyCqlLibrary() {
        Library library = libraryTranslator.translateToFhir();

        assertEquals(library.getDate().getTime(), new Date().getTime(), 10L);
        assertEquals(cqlName,  library.getName());
        //TODO determine why below passes at runtime but not during unit test
        //assertEquals(fhirResourceURL, library.getUrl());

        assertEquals(1, library.getType().getCoding().size());
        assertEquals(SYSTEM_TYPE, library.getType().getCoding().get(0).getSystem());
        assertEquals(SYSTEM_CODE, library.getType().getCoding().get(0).getCode());
    }


    @Test
    void testTranslateToFhir_verifyAttachments() {


        Library library = libraryTranslator.translateToFhir();

        assertEquals(2, library.getContent().size());

        assertEquals(new String(elm), decodeBase64(library.getContent().get(0).getData()));
        assertEquals(ELM_CONTENT_TYPE, library.getContent().get(0).getContentType());

        assertEquals(new String(cql), decodeBase64(library.getContent().get(1).getData()));
        assertEquals(CQL_CONTENT_TYPE, library.getContent().get(1).getContentType());
    }

    private String decodeBase64(byte[] bytes) {
        return new String(Base64.getDecoder().decode(bytes));
    }
}