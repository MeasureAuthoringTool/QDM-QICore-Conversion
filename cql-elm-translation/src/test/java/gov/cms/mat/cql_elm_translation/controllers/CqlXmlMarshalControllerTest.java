package gov.cms.mat.cql_elm_translation.controllers;

import gov.cms.mat.cql_elm_translation.service.MatXmlConversionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CqlXmlMarshalControllerTest {
    @Mock
    private MatXmlConversionService matXmlConversionService;

    @InjectMocks
    private CqlXmlMarshalController cqlXmlMarshalController;

    @Test
    void convertXmlToCql() {
        String xml = "<xml>Howdy</xml>";
        String json = "{}";

        when(matXmlConversionService.processCqlXml(xml)).thenReturn(json);

        String result = cqlXmlMarshalController.convertXmlToCql(xml);
        assertEquals(json, result);

        verify(matXmlConversionService).processCqlXml(xml);
    }
}