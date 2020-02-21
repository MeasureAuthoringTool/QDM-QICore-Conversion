package gov.cms.mat.fhir.services.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean
    @Profile("local")
    public OpenAPI customOpenAPILocal() {
        return create();
    }

    @Bean
    @Profile("!local")
    public OpenAPI customOpenAPI() {
        return create().servers(getServersList());
    }

    private OpenAPI create() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info().title("MAT Fhir API").description(
                        "This is a SpringBoot v2.2.x restful service for converting MAT objects to FHIR."));
    }

    private List<Server> getServersList() {
        List<Server> servers = new ArrayList<>(3);

        // spring-boot locally
        servers.add(createServer("http://localhost:9080/",
                "Running as Spring-boot micro-service locally"));
        // Tomcat locally
        servers.add(createServer("http://localhost:8080/mat-fhir-services-0.0.1-SNAPSHOT/",
                "Running in Tomcat locally"));
        // MAT Dev Secure
        servers.add(createServer("https://matdev.semanticbits.com/mat-fhir-services/",
                "MAT Secure Development Environment"));

        return servers;
    }

    Server createServer(String url, String description) {
        Server server = new Server();
        server.setUrl(url);
        server.setDescription(description);
        return server;
    }
}