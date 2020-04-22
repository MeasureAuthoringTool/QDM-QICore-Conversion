package gov.cms.mat.fhir.services.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties("code-systemlookup-4-1")
@Getter
@Setter
public class CodeSystemLookup {
    private Map<String, String> map;
}
