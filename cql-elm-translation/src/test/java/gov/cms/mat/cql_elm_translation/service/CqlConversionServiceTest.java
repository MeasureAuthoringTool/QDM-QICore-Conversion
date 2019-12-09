package gov.cms.mat.cql_elm_translation.service;

import gov.cms.mat.cql_elm_translation.ResourceFileUtil;
import gov.cms.mat.cql_elm_translation.data.RequestData;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.LibraryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CqlConversionServiceTest implements ResourceFileUtil {
    CqlConversionService cqlConversionService = new CqlConversionService();

    String cqlData;
    LibraryBuilder.SignatureLevel signatureLevel;
    Boolean annotations;
    Boolean locators;
    Boolean disableListDemotion;
    Boolean disableListPromotion;
    Boolean disableMethodInvocation;
    Boolean validateUnits;

    @BeforeEach
    public void setUp() {
        cqlData = getData("/test.cql");
        annotations = Boolean.TRUE;
        locators = Boolean.TRUE;
        disableListDemotion = Boolean.TRUE;
        disableListPromotion = Boolean.TRUE;
        disableMethodInvocation = Boolean.TRUE;
        validateUnits = Boolean.TRUE;
    }

    @Test
    void process_Bad() {
        cqlData = "cqlData";
        CqlTranslator cqlTranslator = buildCqlTranslator();

        assertEquals(2, cqlTranslator.getErrors().size());

        assertTrue(cqlTranslator.toJson().contains("CqlToElmError"));
    }

    @Test
    void process_Good() {
        CqlTranslator cqlTranslator = buildCqlTranslator();
        assertTrue(cqlTranslator.getErrors().isEmpty());
        assertFalse(cqlTranslator.toJson().contains("CqlToElmError"));
    }

    @Test
    void process_SignatureLevelNone() {
        String jsonDefault = getJson();

        signatureLevel = LibraryBuilder.SignatureLevel.None;

        String jsonSignatureLevelNone = getJson();

        // NO change expected null signatureLevel and LibraryBuilder.SignatureLevel.None behave the same
        assertEquals(jsonDefault, jsonSignatureLevelNone);
    }

    @Test
    void process_SignatureLevelAll() {
        String jsonDefault = getJson();

        signatureLevel = LibraryBuilder.SignatureLevel.All;

        String jsonSignatureLevelNone = getJson();

        assertEquals(jsonDefault, jsonSignatureLevelNone); // NO change TODO not expected
    }

    @Test
    void process_annotations() {
        String jsonDefault = getJson();

        annotations = Boolean.FALSE;

        String jsonAnnotations = getJson();

        assertNotEquals(jsonDefault, jsonAnnotations); // data changed
    }

    @Test
    void process_locators() {
        String locatorTag = "\"locator\" : ";

        String jsonDefault = getJson();
        assertTrue(jsonDefault.contains(locatorTag));

        locators = Boolean.FALSE;

        String jsonSignatureLevelNone = getJson();
        assertFalse(jsonSignatureLevelNone.contains(locatorTag));

        assertNotEquals(jsonDefault, jsonSignatureLevelNone); // data changed
    }

    @Test
    void process_disableListPromotion() {
        String jsonDefault = getJson();

        disableListPromotion = Boolean.FALSE;

        String jsonAnnotations = getJson();

        assertEquals(jsonDefault, jsonAnnotations); // NO change TODO not expected
    }

    @Test
    void process_disableMethodInvocation() {
        String jsonDefault = getJson();

        disableMethodInvocation = Boolean.FALSE;

        String jsonAnnotations = getJson();

        assertEquals(jsonDefault, jsonAnnotations); // NO change TODO not expected
    }

    @Test
    void process_validateUnits() {
        String jsonDefault = getJson();

        validateUnits = Boolean.FALSE;

        String jsonAnnotations = getJson();

        assertEquals(jsonDefault, jsonAnnotations); // NO change TODO not expected
    }

    @Test
    void process_disableListDemotion() {
        String jsonDefault = getJson();

        disableListDemotion = Boolean.FALSE;

        String jsonAnnotations = getJson();

        assertEquals(jsonDefault, jsonAnnotations); // NO change TODO not expected
    }

    private String getJson() {
        CqlTranslator cqlTranslator = buildCqlTranslator();

        assertTrue(cqlTranslator.getErrors().isEmpty());

        return cqlTranslator.toJson();
    }

    private CqlTranslator buildCqlTranslator() {
        RequestData requestData = buildRequestData();
        return cqlConversionService.processCqlData(requestData);
    }

    private RequestData buildRequestData() {
        return RequestData.builder()
                .cqlData(cqlData)
                .signatures(signatureLevel)
                .annotations(annotations)
                .locators(locators)
                .disableListDemotion(disableListDemotion)
                .disableListPromotion(disableListPromotion)
                .disableMethodInvocation(disableMethodInvocation)
                .validateUnits(validateUnits)
                .build();
    }
}