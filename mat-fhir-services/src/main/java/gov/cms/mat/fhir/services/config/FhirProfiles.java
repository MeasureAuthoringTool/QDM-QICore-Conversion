package gov.cms.mat.fhir.services.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Stinks to do it this way but we had issues getting all the files in a resource directory with docker.
 */
@EnableConfigurationProperties
@Configuration
@ConfigurationProperties(prefix = "fhir-profiles")
@Data
public class FhirProfiles {
    List<String> profiles;
}