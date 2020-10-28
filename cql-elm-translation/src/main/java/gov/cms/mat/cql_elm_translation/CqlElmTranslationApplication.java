package gov.cms.mat.cql_elm_translation;

import gov.cms.mat.cql_elm_translation.config.security.SecurityFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@Configuration
@Slf4j
public class CqlElmTranslationApplication {

    public static void main(String[] args) {
        SpringApplication.run(CqlElmTranslationApplication.class, args);
    }

    /**
     *  Force UTC timezone locally.
     */
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        log.info("Set timezone to UTC.");
    }

    @Bean(name="FilterRegistrationBeanSecurityFilter")
    public FilterRegistrationBean<SecurityFilter> securityFilter(SecurityFilter securityFilter){
        FilterRegistrationBean<SecurityFilter> registrationBean
                = new FilterRegistrationBean<>(securityFilter);
        registrationBean.setFilter(securityFilter);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
