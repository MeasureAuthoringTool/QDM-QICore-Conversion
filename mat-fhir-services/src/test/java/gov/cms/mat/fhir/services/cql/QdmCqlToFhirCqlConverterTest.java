package gov.cms.mat.fhir.services.cql;

import gov.cms.mat.fhir.services.ResourceFileUtil;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class QdmCqlToFhirCqlConverterTest implements ResourceFileUtil {

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    void convert() {
//        QdmQiCoreDataService  qdmQicoreDataService = new QdmQiCoreDataService(restTemplate);
//        ReflectionTestUtils.setField(qdmQicoreDataService, "baseURL", "http://localhost:9090");
//        String sourceCql  =  getStringFromResource("/bad.cql");
//        QdmCqlToFhirCqlConverter qdmCqlToFhirCqlConverter = new QdmCqlToFhirCqlConverter(sourceCql, qdmQicoreDataService);
//        String  convertedCql = qdmCqlToFhirCqlConverter.convert();
//        assertNotNull(convertedCql);
//
//        System.out.println(convertedCql);
    }
}