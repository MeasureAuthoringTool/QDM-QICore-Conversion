package gov.cms.mat.qdmqicore.mapping;

import gov.cms.mat.qdmqicore.mapping.config.security.SecurityFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@EnableScheduling
@SpringBootApplication
@Configuration
@Slf4j
public class Application extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(
            SpringApplicationBuilder builder) {
        return builder.sources(Application.class);
    }

    /**
     * Force UTC timezone locally.
     */
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        log.info("Set timezone to UTC.");
    }

    @Bean(name = "FilterRegistrationBeanSecurityFilter")
    public FilterRegistrationBean<SecurityFilter> securityFilter(SecurityFilter securityFilter) {
        FilterRegistrationBean<SecurityFilter> registrationBean
                = new FilterRegistrationBean<>(securityFilter);
        registrationBean.setFilter(securityFilter);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}