package gov.cms.mat.fhir.services.config.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.MappedInterceptor;

/**
 * Class creates the interceptors used in header processing
 */
@Configuration
@Slf4j
public class InterceptorConfig implements WebMvcConfigurer {

    /**
     * Create the Request Interceptor
     *
     * @return the Interceptor used for all incoming servlet request
     */
    @Bean
    public MappedInterceptor requestInterceptor() {
        //https://stackoverflow.com/questions/46953039/spring-interceptor-not-working-in-spring-data-rest-urls
        return new MappedInterceptor(new String[]{"/**"}, new RequestHeaderInterceptor());
    }
}





