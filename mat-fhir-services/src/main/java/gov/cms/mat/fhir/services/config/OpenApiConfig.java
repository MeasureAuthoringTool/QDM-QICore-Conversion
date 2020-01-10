package gov.cms.mat.fhir.services.config;



import io.swagger.v3.oas.annotations.servers.Servers;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(getServersList())
                .components(new Components())
                .info(new Info().title("MAT Fhir API").description(
                        "This is a SpringBoot v2.2.x restful service for converting MAT objects to FHIR."));             
    }
    
    private List<Server> getServersList() {
        
        List<Server> servers = new ArrayList<Server>();
        // spring-boot locally
        Server localMicroService = new Server();
        localMicroService.setUrl("http://localhost:9080/");
        localMicroService.setDescription("Running as Spring-boot micro-service locally");
        servers.add(localMicroService);
        // Tomcat locally
        Server tomcatMicroService = new Server();
        localMicroService.setUrl("http://localhost:8080/mat-fhir-services-0.0.1-SNAPSHOT/");
        localMicroService.setDescription("Running in Tomcat locally");
        servers.add(tomcatMicroService);
        // MAT Dev Secure
        Server matDev = new Server();
        localMicroService.setUrl("https://matdev.semanticbits.com/mat-fhir-services/");
        localMicroService.setDescription("MAT Secure Development Environment");
        servers.add(matDev);
        
        return servers;
    }
}