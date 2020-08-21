package gov.cms.mat.fhir.services.config.health;

import gov.cms.mat.fhir.services.hapi.HapiFhirLinkProcessor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Optional;

@Component
@Slf4j
public class HapiFhirService extends HealthCheckerBase {
    private final HapiFhirLinkProcessor hapiFhirLinkProcessor;

    @Value("${fhir.r4.baseurl}")
    private String baseURL;

    public HapiFhirService(HapiFhirLinkProcessor hapiFhirLinkProcessor) {
        this.hapiFhirLinkProcessor = hapiFhirLinkProcessor;
    }

    @Override
    Health check() {
        Status status = checkHapiFhirServer();

        if (status == Status.UP) {
            return Health.up().build();
        } else {
            return Health.status(status).build();
        }
    }

    private Status checkHapiFhirServer() {
        String bundleUrl = baseURL + "/" + "Library";
        Optional<Bundle> optionalBundle = hapiFhirLinkProcessor.fetchBundleByUrl(bundleUrl);

        if (optionalBundle.isPresent()) {
            if (CollectionUtils.isEmpty(optionalBundle.get().getEntry())) {
                log.warn("No Libraries were found in hapi, please load the HapiGlobalCommonLibs");
            }

            return Status.UP;
        } else {
            return Status.DOWN;
        }
    }
}
