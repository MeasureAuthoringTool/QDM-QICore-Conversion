package gov.cms.mat.fhir.services.config.health;

import gov.cms.mat.fhir.services.exceptions.SelfHealthCheckException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class SelfHealthCheckingService {
    private final RestTemplate restTemplate;
    @Value("${self.health.baseurl}")
    private String baseUrl;

    public SelfHealthCheckingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Health check() {
        Health health = new SelfHealthChecker().check();
        log.info("Self health is: {}", health.getStatus());
        return health;
    }

    public void checkHealthWithException() {
        Health health = check();

        if (health != null && health.getStatus() != null && health.getStatus().equals(Status.UP)) {
            log.info("Self health passed");
        } else {
            throw new SelfHealthCheckException(health == null ? null : health.getStatus());
        }
    }

    private class SelfHealthChecker extends SpringBootHealthChecker {
        protected SelfHealthChecker() {
            super(restTemplate);
        }

        @Override
        String getBaseUrl() {
            return baseUrl;
        }
    }
}