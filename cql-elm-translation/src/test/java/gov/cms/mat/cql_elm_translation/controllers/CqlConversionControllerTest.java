package gov.cms.mat.cql_elm_translation.controllers;

import gov.cms.mat.cql_elm_translation.service.CqlConversionService;
import org.cqframework.cql.cql2elm.LibraryBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CqlConversionControllerTest {
    @Mock
    private CqlConversionService cqlConversionService;

    @InjectMocks
    private CqlConversionController cqlConversionController;

    @Test
    void cqlToElmJson() {
        String result = "json-data";
        when(cqlConversionService.processCqlData(any())).thenReturn(result);

        String json = cqlConversionController.cqlToElmJson("cqlData",
                LibraryBuilder.SignatureLevel.All,
                "restrictDataModel",
                true,
                true,
                true,
                true,
                true,
                true);

        assertEquals(result, json);
        verify(cqlConversionService).processCqlData(any());
    }
}