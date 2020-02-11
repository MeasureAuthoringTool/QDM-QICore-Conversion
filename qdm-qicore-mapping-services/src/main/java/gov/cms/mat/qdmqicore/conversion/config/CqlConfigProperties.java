package gov.cms.mat.qdmqicore.conversion.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@EnableConfigurationProperties
@Configuration
@ConfigurationProperties(prefix = "cql")
@Data
public class CqlConfigProperties {
    List<String> negations;
}
