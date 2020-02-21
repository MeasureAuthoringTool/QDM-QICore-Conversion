package gov.cms.mat.fhir.services.config.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class QdmQicoreMappingService extends SpringBootHealthChecker {
    @Value("${qdmqicore.conversion.baseurl}")
    private String baseURL;

    public QdmQicoreMappingService(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    String getBaseUrl() {
        return baseURL;
    }
}