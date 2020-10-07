package gov.cms.mat.fhir.services.config;

import gov.cms.mat.fhir.services.config.logging.RequestResponseLoggingExternalInterceptor;
import gov.cms.mat.fhir.services.config.logging.RequestResponseLoggingInterceptor;
import gov.cms.mat.fhir.services.config.logging.RequestResponseLoggingMdcInternalInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class RestTemplateConfig {

   private final RequestResponseLoggingMdcInternalInterceptor requestResponseLoggingMdcInternalInterceptor;

    public RestTemplateConfig(RequestResponseLoggingMdcInternalInterceptor requestResponseLoggingMdcInternalInterceptor) {
        this.requestResponseLoggingMdcInternalInterceptor = requestResponseLoggingMdcInternalInterceptor;
    }

    @Bean(name = "internalRestTemplate")
    @Primary
    public RestTemplate restTemplateInternal(RestTemplateBuilder builder) {
        return getRestTemplate(requestResponseLoggingMdcInternalInterceptor);
    }

    @Bean(name = "externalRestTemplate")
    public RestTemplate restTemplateExternal(RestTemplateBuilder builder) {
        return getRestTemplate(new RequestResponseLoggingExternalInterceptor());
    }

    public RestTemplate getRestTemplate(RequestResponseLoggingInterceptor interceptor) {
        SimpleClientHttpRequestFactory requestFactory =   new SimpleClientHttpRequestFactory();
        requestFactory.setOutputStreaming(false);

        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(requestFactory);
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate
                .setInterceptors(List.of(interceptor));
        return restTemplate;
    }
}