package gov.mat.hapi;

import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.to.FhirTesterMvcConfig;
import ca.uhn.fhir.to.TesterConfig;
import ca.uhn.fhir.to.mvc.AnnotationMethodHandlerAdapterConfigurer;
import ca.uhn.fhir.to.util.WebUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@Configuration
@Slf4j
@ComponentScan(basePackages={"ca.uhn.fhir.to"}, excludeFilters={
        @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value= FhirTesterMvcConfig.class)})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Value("${hapi.fhir.server.url}")
    private String hapiFhirUrl;

    /**
     *  Force UTC timezone locally.
     */
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        log.info("Set timezone to UTC.");
    }

    @Bean
    public TesterConfig testerConfig() {
        TesterConfig retVal = new TesterConfig();
        retVal
                .addServer()
                .withId("Hapi-Fhir")
                .withFhirVersion(FhirVersionEnum.R4)
                .withBaseUrl(hapiFhirUrl)
                .withName("Hapi-Fhir");
        retVal.setRefuseToFetchThirdPartyUrls(false);
        return retVal;
    }
    @Bean
    public WebMvcConfigurer configurer () {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                WebUtil.webJarAddBoostrap3(registry);
                WebUtil.webJarAddJQuery(registry);
                WebUtil.webJarAddFontAwesome(registry);
                WebUtil.webJarAddJSTZ(registry);
                WebUtil.webJarAddEonasdanBootstrapDatetimepicker(registry);
                WebUtil.webJarAddMomentJS(registry);
                WebUtil.webJarAddSelect2(registry);
                WebUtil.webJarAddAwesomeCheckbox(registry);
                registry.addResourceHandler(new String[]{"/css/**"}).addResourceLocations(new String[]{"classpath:/css/"});
                registry.addResourceHandler(new String[]{"/fa/**"}).addResourceLocations(new String[]{"classpath:/fa/"});
                registry.addResourceHandler(new String[]{"/fonts/**"}).addResourceLocations(new String[]{"classpath:/fonts/"});
                registry.addResourceHandler(new String[]{"/img/**"}).addResourceLocations(new String[]{"classpath:/img/"});
                registry.addResourceHandler(new String[]{"/js/**"}).addResourceLocations(new String[]{"classpath:/js/"});
            }
        };
    }

    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("classpath:/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        return resolver;
    }

    @Bean
    public AnnotationMethodHandlerAdapterConfigurer annotationMethodHandlerAdapterConfigurer() {
        return new AnnotationMethodHandlerAdapterConfigurer();
    }

    @Bean
    public ThymeleafViewResolver viewResolver() {
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(this.templateEngine());
        viewResolver.setCharacterEncoding("UTF-8");
        return viewResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(this.templateResolver());
        return templateEngine;
    }
}