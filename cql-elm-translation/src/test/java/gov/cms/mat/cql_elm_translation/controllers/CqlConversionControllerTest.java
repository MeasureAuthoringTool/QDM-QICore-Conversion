package gov.cms.mat.cql_elm_translation.controllers;

import gov.cms.mat.cql_elm_translation.ResourceFileUtil;
import gov.cms.mat.cql_elm_translation.service.CqlConversionService;
import gov.cms.mat.cql_elm_translation.service.MatXmlConversionService;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CqlConversionControllerTest implements ResourceFileUtil {
    private final static String translatorOptionsTag = "\"translatorOptions\"";

    @Mock
    private CqlConversionService cqlConversionService;
    @Mock
    private MatXmlConversionService matXmlConversionService;
    @Mock
    private CqlTranslator cqlTranslator;

    @InjectMocks
    private CqlConversionController cqlConversionController;

//    @Test
//    void cqlToElmJson() {
//        String cqlData = "cqlData";
//        String result = "json-data";
//        when(cqlConversionService.processCqlDataWithErrors(any())).thenReturn(result);
//
//        String json = cqlConversionController.cqlToElmJson(cqlData,
//                LibraryBuilder.SignatureLevel.All,
//                true,
//                true,
//                true,
//                true,
//                true,
//                true);
//
//        assertEquals(result, json);
//        verify(cqlConversionService).processCqlDataWithErrors(any());
//        //verify(cqlConversionService).processQdmVersion(cqlData);
//    }

//    @Test
//    void xmlToElmJson() {
//        String cqlData = "cqlData";
//        String xml = "</xml>";
//        String result = "json-data";
//
//        when(matXmlConversionService.processCqlXml(xml)).thenReturn(cqlData);
//        when(cqlConversionService.processCqlDataWithErrors(any())).thenReturn(result);
//
//        String json = cqlConversionController.xmlToElmJson(xml,
//                LibraryBuilder.SignatureLevel.All,
//                true,
//                true,
//                true,
//                true,
//                true,
//                true);
//
//        assertEquals(result, json);
//
//        verify(matXmlConversionService).processCqlXml(xml);
//        verify(cqlConversionService).processCqlDataWithErrors(any());
//        //verify(cqlConversionService).processQdmVersion(cqlData);
//    }

    @Test
    void translatorOptionsRemoverNoErrors() {
        String json = getData("/fhir4_std_lib_no_errors.json");

        assertTrue(json.contains(translatorOptionsTag));

        CqlConversionController.TranslatorOptionsRemover translatorOptionsRemover =
                new CqlConversionController.TranslatorOptionsRemover(json);

        String cleaned = translatorOptionsRemover.clean();

        assertFalse(cleaned.contains(translatorOptionsTag));
    }

    @Test
    void translatorOptionsRemoverErrors() {

        String json = getData("/fhir4_std_lib_errors.json");

        assertTrue(json.contains(translatorOptionsTag));

        CqlConversionController.TranslatorOptionsRemover translatorOptionsRemover =
                new CqlConversionController.TranslatorOptionsRemover(json);

        String cleaned = translatorOptionsRemover.clean();

        assertFalse(cleaned.contains(translatorOptionsTag));
    }
}