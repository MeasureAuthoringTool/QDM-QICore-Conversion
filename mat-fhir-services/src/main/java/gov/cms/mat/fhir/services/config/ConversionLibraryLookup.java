package gov.cms.mat.fhir.services.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

@Component
@ConfigurationProperties("conversion-lib-lookup-4-1")
@Getter
@Setter
public class ConversionLibraryLookup {
    private LinkedHashMap<String, String> map;
}
