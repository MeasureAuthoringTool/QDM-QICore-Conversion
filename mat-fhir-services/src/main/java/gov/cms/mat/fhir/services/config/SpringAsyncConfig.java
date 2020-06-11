package gov.cms.mat.fhir.services.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class SpringAsyncConfig {

    @Value("${validation-core-pool-size}")
    int validationPoolCoreSize;

    @Value("${valueset-vsac-validation-core-pool-size}")
    int valuesetVsacValidationCorePoolSize;

    @Value("${code-system-vsac-validation-core-pool-size}")
    int codesSystemVsacValidationCorePoolSize;

    @Bean(name = "threadPoolValidation")
    public ThreadPoolTaskExecutor threadPoolValidation() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(validationPoolCoreSize);
        executor.setMaxPoolSize(validationPoolCoreSize * 5);
        executor.setQueueCapacity(validationPoolCoreSize * 10);
        executor.setThreadNamePrefix("FHIR-ASYNC-");

        executor.initialize();

        return executor;
    }

    @Bean(name = "valueSetTheadPoolValidation")
    public ThreadPoolTaskExecutor valueSetThreadPoolValidation() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(valuesetVsacValidationCorePoolSize);
        executor.setMaxPoolSize(valuesetVsacValidationCorePoolSize);
        executor.setQueueCapacity(valuesetVsacValidationCorePoolSize * 10);
        executor.setThreadNamePrefix("V-SET-ASYNC-");

        executor.initialize();

        return executor;
    }

    @Bean(name = "codeSystemTheadPoolValidation")
    public ThreadPoolTaskExecutor codeSystemThreadPoolValidation() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(codesSystemVsacValidationCorePoolSize);
        executor.setMaxPoolSize(codesSystemVsacValidationCorePoolSize);
        executor.setQueueCapacity(codesSystemVsacValidationCorePoolSize * 10);
        executor.setThreadNamePrefix("CODE-SYSTEM-ASYNC-");

        executor.initialize();

        return executor;
    }


}
