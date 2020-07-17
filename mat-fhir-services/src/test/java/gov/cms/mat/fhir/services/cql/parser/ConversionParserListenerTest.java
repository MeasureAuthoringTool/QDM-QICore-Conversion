package gov.cms.mat.fhir.services.cql.parser;

import gov.cms.mat.fhir.services.ResourceFileUtil;
import gov.cms.mat.fhir.services.components.conversion.ConversionDataComponent;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

class ConversionParserListenerTest implements ResourceFileUtil {

    @Test
    void parse() {
        String cql = getStringFromResource("/mike.cql");

        RestTemplate restTemplate  = new RestTemplate();
        MappingSpreadsheetService mappingSpreadsheetService = new MappingSpreadsheetService(restTemplate);
        ReflectionTestUtils.setField(mappingSpreadsheetService, "conversionUrl", "http://localhost:9090");
        ConversionDataComponent conversionDataComponent = new ConversionDataComponent(mappingSpreadsheetService);

        ConversionParserListener parserListener = new ConversionParserListener(conversionDataComponent);
        parserListener.parse(cql);
    }
}