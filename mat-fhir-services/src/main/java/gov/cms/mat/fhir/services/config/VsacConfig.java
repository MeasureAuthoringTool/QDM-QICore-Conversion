package gov.cms.mat.fhir.services.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration

@ConfigurationProperties(prefix = "vsac-client")
@Data
public class VsacConfig {
   // private static String PROXY_HOST = null;
   // private static int PROXY_PORT = 0;

    private String server;
    private String service;
    private String retrieveMultiOidsService;

    private String profileService;
    private String versionService;
    private String vsacServerDrcUrl;
    private String authCLass;
}
