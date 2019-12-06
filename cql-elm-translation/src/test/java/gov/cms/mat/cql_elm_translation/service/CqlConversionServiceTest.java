package gov.cms.mat.cql_elm_translation.service;

import gov.cms.mat.cql_elm_translation.ResourceFileUtil;
import gov.cms.mat.cql_elm_translation.data.RequestData;
import org.cqframework.cql.cql2elm.LibraryBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CqlConversionServiceTest implements ResourceFileUtil {
    CqlConversionService cqlConversionService = new CqlConversionService();

    @Test
    void process_bad() {
        RequestData requestData = RequestData.builder()
                .cqlData("cqlData")
                .signatures(LibraryBuilder.SignatureLevel.All)
                .restrictDataModel("restrictDataModel")
                .annotations(true)
                .locators(true)
                .disableListDemotion(true)
                .disableListPromotion(true)
                .disableMethodInvocation(true)
                .validateUnits(true)
                .build();

        String json = cqlConversionService.processCqlData(requestData);
        assertTrue(json.contains("CqlToElmError"));
    }

    @Test
    void process_good() {
        String cql = getData("/test.cql");

        RequestData requestData = RequestData.builder()
                .cqlData(cql)
                .signatures(LibraryBuilder.SignatureLevel.All)
                .restrictDataModel("restrictDataModel")
                .annotations(true)
                .locators(true)
                .disableListDemotion(true)
                .disableListPromotion(true)
                .disableMethodInvocation(true)
                .validateUnits(true)
                .build();

        String json = cqlConversionService.processCqlData(requestData);
        assertFalse(json.contains("CqlToElmError"));
    }
}