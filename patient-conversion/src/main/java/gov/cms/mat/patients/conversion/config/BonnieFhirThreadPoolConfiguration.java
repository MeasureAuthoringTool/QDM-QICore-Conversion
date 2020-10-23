package gov.cms.mat.patients.conversion.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties("bonnie-thread-pools") // prefix app, find app.* values
public class BonnieFhirThreadPoolConfiguration {

    @Getter
    @Setter
    private List<ThreadPoolConfigurations> threadPoolConfigurations = new ArrayList<>();


    @Data
    static class ThreadPoolConfigurations {
        private String name;
        private Integer corePoolSize;
        private Integer maxPoolSize;
        private Integer queueCapacity;
    }
}
