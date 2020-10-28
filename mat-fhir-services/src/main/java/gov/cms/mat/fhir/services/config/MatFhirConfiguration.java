package gov.cms.mat.fhir.services.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties("mat-fhir") // prefix app, find app.* values
public class MatFhirConfiguration {

    private List<ThreadPoolConfigurations> threadPoolConfigurations = new ArrayList<>();

    public List<ThreadPoolConfigurations> getThreadPoolConfigurations() {
        return threadPoolConfigurations;
    }

    public void setThreadPoolConfigurations(List<ThreadPoolConfigurations> threadPoolConfigurations) {
        this.threadPoolConfigurations = threadPoolConfigurations;
    }

    @Data
    static class ThreadPoolConfigurations {
        private String name;
        private Integer corePoolSize;
        private Integer maxPoolSize;
        private Integer queueCapacity;
    }
}
