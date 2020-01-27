package gov.cms.mat.fhir.services.config.health;

import gov.cms.mat.fhir.services.exceptions.HealthNullStatusException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.web.client.RestTemplate;

@Slf4j
public abstract class SpringBootHealthChecker extends HealthCheckerBase {
    private final RestTemplate restTemplate;

    protected SpringBootHealthChecker(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    Health check() {
        HealthJson healthFromRest = fetchSpringBootMicroServiceHealth();

        if (healthFromRest == null || healthFromRest.getStatus() == null) {
            throw new HealthNullStatusException();
        } else if (healthFromRest.getStatus() == Status.UP) {
            return Health.up().build();
        } else {
            return Health.status(healthFromRest.getStatus()).build();
        }
    }

    private HealthJson fetchSpringBootMicroServiceHealth() {
        String actuatorHealthUrl = getBaseUrl() + "/actuator/health";
        log.debug("{} actuatorHealthUrl: {}", getClass().getSimpleName(), actuatorHealthUrl);

        return restTemplate.getForObject(actuatorHealthUrl, HealthJson.class);
    }

    abstract String getBaseUrl();

    @Data
    private static class HealthJson {
        Status status;
    }
}
