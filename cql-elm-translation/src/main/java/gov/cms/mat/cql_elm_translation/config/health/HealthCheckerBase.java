package gov.cms.mat.cql_elm_translation.config.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

@Slf4j
public abstract class HealthCheckerBase implements HealthIndicator {
    @Override
    public Health health() {
        try {
            Health health = check();
            log.debug("Health for {} is {}", getClass().getSimpleName(), health.getStatus());
            return health;
        } catch (Exception e) {
            log.info("Health check for {} failed with error", getClass().getSimpleName(), e);
            return Health.down(e).build();
        }
    }

    abstract Health check();
}
