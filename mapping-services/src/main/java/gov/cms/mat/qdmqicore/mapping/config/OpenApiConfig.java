package gov.cms.mat.qdmqicore.mapping.config;

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
    @Value("${swagger-server}")
    private String swaggerServer;


    @Bean
    public OpenAPI customOpenAPI() {
        OpenAPI openAPI = new OpenAPI()
                .info(buildInfo());

        if(StringUtils.isNotEmpty(swaggerServer)) {
            log.info("Setting swagger server to: {}", swaggerServer);
            openAPI.addServersItem( new Server().url(swaggerServer));
        }

        return openAPI;
    }


    private Info buildInfo() {
        return new Info()
                .title("Mapping services API")
                .description("This is a SpringBoot v2.3.x restful service for obtaining data from google spreadsheets.");
    }
}