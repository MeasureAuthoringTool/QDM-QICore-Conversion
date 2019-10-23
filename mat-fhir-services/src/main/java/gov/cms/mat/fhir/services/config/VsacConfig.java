package gov.cms.mat.fhir.services.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration

@ConfigurationProperties(prefix = "vsac-client")
@Data
public class VsacConfig {
    String proxyHost = null;
    int proxyPort = 0;

    private String server;
    private String service;
    private String retrieveMultiOidsService;

    private String profileService;
    private String versionService;
    private String vsacServerDrcUrl;
}
