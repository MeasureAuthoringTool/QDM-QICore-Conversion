package gov.cms.mat.fhir.services.config;

import gov.cms.mat.fhir.services.config.logging.RequestResponseLoggingExternalInterceptor;
import gov.cms.mat.fhir.services.config.logging.RequestResponseLoggingInterceptor;
import gov.cms.mat.fhir.services.config.logging.RequestResponseLoggingMdcInternalInterceptor;
import gov.cms.mat.vsac.RefreshTokenManagerImpl;
import gov.cms.mat.vsac.VsacService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.inject.Named;
import java.util.List;

@Configuration
@Component
public class RestTemplateConfig {
    @Value("${vsac.ticket-url-base}")
    private String ticketUrlBase;
    @Value("${vsac.url-base}")
    private String vsacUrlBase;

   private final RequestResponseLoggingMdcInternalInterceptor requestResponseLoggingMdcInternalInterceptor;

    public RestTemplateConfig(RequestResponseLoggingMdcInternalInterceptor requestResponseLoggingMdcInternalInterceptor) {
        this.requestResponseLoggingMdcInternalInterceptor = requestResponseLoggingMdcInternalInterceptor;
    }

    @Bean(name = "internalRestTemplate")
    @Primary
    public RestTemplate restTemplateInternal() {
        return getRestTemplate(requestResponseLoggingMdcInternalInterceptor);
    }

    @Bean(name = "externalRestTemplate")
    public RestTemplate restTemplateExternal() {
        return getRestTemplate(new RequestResponseLoggingExternalInterceptor());
    }

    @Bean
    public VsacService vsacService(@Named("externalRestTemplate") RestTemplate restTemplate) {
        return new VsacService(ticketUrlBase,vsacUrlBase,restTemplate, RefreshTokenManagerImpl.getInstance());
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