package gov.cms.mat.fhir.services;

import ca.uhn.fhir.context.FhirContext;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

public interface HapiFhirServerTest {

    default HapiFhirServer createTestHapiServer() {
        HapiFhirServer hapiFhirServer = new HapiFhirServer(FhirContext.forR4(), new RestTemplate());
        ReflectionTestUtils.setField(hapiFhirServer, "baseURL", "http://localhost:6060/hapi-fhir-jpaserver/fhir/");
        hapiFhirServer.setUp();

        return hapiFhirServer;
    }

}
