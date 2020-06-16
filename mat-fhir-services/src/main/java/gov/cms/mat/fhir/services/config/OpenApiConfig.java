package gov.cms.mat.fhir.services.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class OpenApiConfig {
    @Value("${swagger-server:#{null}}")
    private String swaggerServer;

    @Bean
    public OpenAPI customOpenAPI() {
        OpenAPI openAPI = new OpenAPI()
                .info(buildInfo());

        if (StringUtils.isNotEmpty(swaggerServer) && !swaggerServer.equals("null")) {
            log.info("Setting swagger server to: {}", swaggerServer);
            openAPI.addServersItem(new Server().url(swaggerServer));
        }

        return openAPI;
    }

    private Info buildInfo() {
        return new Info()
                .title("MAT Fhir API")
                .description("This is a SpringBoot v2.3.x restful service for converting MAT objects to FHIR.");
    }
}