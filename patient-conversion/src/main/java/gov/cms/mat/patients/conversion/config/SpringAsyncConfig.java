package gov.cms.mat.patients.conversion.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@Slf4j
public class SpringAsyncConfig {
    private final BonnieFhirThreadPoolConfiguration bonnieFhirThreadPoolConfiguration;

    public SpringAsyncConfig(BonnieFhirThreadPoolConfiguration bonnieFhirThreadPoolConfiguration) {
        this.bonnieFhirThreadPoolConfiguration = bonnieFhirThreadPoolConfiguration;
    }

    @Bean(name = "threadPoolConversion")
    public ThreadPoolTaskExecutor threadPoolConversion() {
        return createThreadPool("threadPoolConversion", "CONVERSION-ASYNC-");
    }

    private BonnieFhirThreadPoolConfiguration.ThreadPoolConfigurations findConfiguration(String name) {
        return bonnieFhirThreadPoolConfiguration.getThreadPoolConfigurations().stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot find ThreadPoolConfiguration with name: " + name));
    }

    private ThreadPoolTaskExecutor createThreadPool(String name, String prefix) {
        BonnieFhirThreadPoolConfiguration.ThreadPoolConfigurations configuration = findConfiguration(name);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(configuration.getCorePoolSize());
        executor.setMaxPoolSize(configuration.getMaxPoolSize());
        executor.setQueueCapacity(configuration.getQueueCapacity());
        executor.setThreadNamePrefix(prefix);

        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.initialize();

        log.info("Created -> {}", configuration);

        return executor;
    }
}
