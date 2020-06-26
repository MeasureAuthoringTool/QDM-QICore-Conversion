package gov.cms.mat.fhir.services.config;

import gov.cms.mat.vsac.VsacRestClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

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

    private boolean useCache; // By saving the data will speed things up for testing.
    private String cacheDirectory;

    private boolean useCacheOnly = true; // Will only look in cache.

    private VsacRestClient vsacRestClient;

    @PostConstruct
    void postConstruct() {
        buildClient();
    }

    private void buildClient() {
        vsacRestClient = new VsacRestClient(getProxyHost(),
                getProxyPort(),
                getServer(),
                getService(),
                getRetrieveMultiOidsService(),
                getProfileService(),
                getVersionService(),
                getVsacServerDrcUrl());
    }
}
