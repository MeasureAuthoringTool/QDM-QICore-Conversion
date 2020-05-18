package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import org.hl7.fhir.r4.model.Library;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.Date;

import static gov.cms.mat.fhir.services.translate.MatLibraryTranslator.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


class MatLibraryTranslatorTest implements IdGenerator {
    private String MEASURE_ID;
    private byte[] cql;
    private byte[] elmJsom;
    private byte[] elmXml;
    private CqlLibrary cqlLib;
    private String baseURL;
    private String fhirResourceURL;
    private String cqlName;
    private MatLibraryTranslator matLibraryTranslator;

    @BeforeEach
    void setUp() {
        MEASURE_ID = createId();
        cqlLib = new CqlLibrary();
        cqlLib.setMeasureId(MEASURE_ID);
        cqlLib.setVersion(new BigDecimal("0.0001"));
        cqlLib.setFinalizedDate(new Date());
        cqlLib.setQdmVersion("v5.5");
        cqlName = "Test CQL Library";
        cqlLib.setCqlName(cqlName);
        cql = "test cql".getBytes();
        elmJsom = "test elm json".getBytes();
        elmXml = "test elm xml".getBytes();
        baseURL = "http://localhost:8080/hapi-fhir-jpaserver/fhir/";
        fhirResourceURL = baseURL + "Library/" + MEASURE_ID;
        System.out.println(fhirResourceURL);
        matLibraryTranslator = new MatLibraryTranslator(cqlLib, cql, elmJsom, elmXml,baseURL, MEASURE_ID);
    }

    @Test
    void testTranslateToFhir_verifyCqlLibrary() {
        Library library = matLibraryTranslator.translateToFhir(null);

        assertEquals(library.getDate().getTime(), new Date().getTime(), 10L);
        assertEquals(cqlName,  library.getName());

        assertEquals(1, library.getType().getCoding().size());
        assertEquals(SYSTEM_TYPE, library.getType().getCoding().get(0).getSystem());
        assertEquals(SYSTEM_CODE, library.getType().getCoding().get(0).getCode());
    }


    @Test
    void testTranslateToFhir_verifyAttachments() {
        Library library = matLibraryTranslator.translateToFhir(null);

        assertEquals(3, library.getContent().size());

        assertEquals(new String(elmJsom), decodeBase64(library.getContent().get(0).getData()));
        assertEquals(JSON_ELM_CONTENT_TYPE, library.getContent().get(0).getContentType());

        assertEquals(new String(cql), decodeBase64(library.getContent().get(1).getData()));
        assertEquals(CQL_CONTENT_TYPE, library.getContent().get(1).getContentType());

        assertEquals(new String(elmXml), decodeBase64(library.getContent().get(2).getData()));
        assertEquals(XML_ELM_CONTENT_TYPE, library.getContent().get(2).getContentType());
    }

    private String decodeBase64(byte[] bytes) {
        return new String(Base64.getDecoder().decode(bytes));
    }
}