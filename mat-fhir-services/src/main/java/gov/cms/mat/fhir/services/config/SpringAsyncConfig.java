package gov.cms.mat.fhir.services.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class SpringAsyncConfig {
    private final MatFhirConfiguration matFhirConfiguration;

    public SpringAsyncConfig(MatFhirConfiguration matFhirConfiguration) {
        this.matFhirConfiguration = matFhirConfiguration;
    }

    @Bean(name = "threadPoolValidation")
    public ThreadPoolTaskExecutor threadPoolValidation() {
        MatFhirConfiguration.ThreadPoolConfigurations configuration = findConfiguration("threadPoolValidation");

        return createThreadPool(configuration, "FHIR-ASYNC-");
    }

    @Bean(name = "valueSetTheadPoolValidation")
    public ThreadPoolTaskExecutor valueSetThreadPoolValidation() {
        MatFhirConfiguration.ThreadPoolConfigurations configuration =
                findConfiguration("valueSetTheadPoolValidation");

        return createThreadPool(configuration, "V-SET-ASYNC-");
    }

    @Bean(name = "codeSystemTheadPoolValidation")
    public ThreadPoolTaskExecutor codeSystemThreadPoolValidation() {
        MatFhirConfiguration.ThreadPoolConfigurations configuration =
                findConfiguration("codeSystemTheadPoolValidation");

        return createThreadPool(configuration, "V-SET-ASYNC-");
    }

    private MatFhirConfiguration.ThreadPoolConfigurations findConfiguration(String name) {
        return matFhirConfiguration.getThreadPoolConfigurations().stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot find ThreadPoolConfiguration with name: " + name));
    }


    private ThreadPoolTaskExecutor createThreadPool(MatFhirConfiguration.ThreadPoolConfigurations configuration,
                                                    String prefix) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(configuration.getCorePoolSize());
        executor.setMaxPoolSize(configuration.getMaxPoolSize());
        executor.setQueueCapacity(configuration.getQueueCapacity());
        executor.setThreadNamePrefix(prefix);

        executor.initialize();

        return executor;
    }
}
