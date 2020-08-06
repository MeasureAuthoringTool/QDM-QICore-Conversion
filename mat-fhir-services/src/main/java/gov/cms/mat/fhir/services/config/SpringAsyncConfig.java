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
        return createThreadPool("threadPoolValidation", "VALIDATION-ASYNC-");
    }

    @Bean(name = "valueSetTheadPoolValidation")
    public ThreadPoolTaskExecutor valueSetThreadPoolValidation() {
        return createThreadPool("valueSetTheadPoolValidation", "VAL-SET-ASYNC-");
    }

    @Bean(name = "codeSystemTheadPoolValidation")
    public ThreadPoolTaskExecutor codeSystemThreadPoolValidation() {
        return createThreadPool("codeSystemTheadPoolValidation", "CODE-SYS-ASYNC-");
    }

    private MatFhirConfiguration.ThreadPoolConfigurations findConfiguration(String name) {
        return matFhirConfiguration.getThreadPoolConfigurations().stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot find ThreadPoolConfiguration with name: " + name));
    }

    private ThreadPoolTaskExecutor createThreadPool(String name, String prefix) {
        MatFhirConfiguration.ThreadPoolConfigurations configuration = findConfiguration(name);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(configuration.getCorePoolSize());
        executor.setMaxPoolSize(configuration.getMaxPoolSize());
        executor.setQueueCapacity(configuration.getQueueCapacity());
        executor.setThreadNamePrefix(prefix);

        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.initialize();

        return executor;
    }
}
