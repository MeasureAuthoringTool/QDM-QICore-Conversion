package gov.cms.mat.fhir.services.cql.parser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gov.cms.mat.fhir.services.exceptions.CqlParseException;
import lombok.Data;
import lombok.NoArgsConstructor;

@Service
public class MappingSpreadsheetService {

    private static final String RESOURCE_DEFINITION = "/resourceDefinition";

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    public static class ResourceDefinition {
        private String elementId;
        private String definition;
        private String cardinality;
        private String type;
        private String isSummary;
        private String isModifier;
    }

    @Value("${qdmqicore.conversion.baseurl}")
    private String fhirMatServicesUrl;

    @Resource
    private MappingSpreadsheetService self;

    private RestTemplate restTemplate;

    public MappingSpreadsheetService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Cacheable("spreadSheetfhirTypes")
    public List<String> getFhirTypes() {
        return self.resourceDefinitions().stream().
                filter(r -> StringUtils.isNotBlank(r.getElementId()) &&
                        StringUtils.contains(r.getElementId(), '.')).
                map(r -> r.getElementId().substring(0, r.getElementId().lastIndexOf("."))).
                distinct().
                sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
    }

    @Cacheable("spreadSheetResourceDefinitions")
    public List<ResourceDefinition> resourceDefinitions() {
        ResponseEntity<ResourceDefinition[]> response;
        try {
            response = restTemplate.getForEntity(fhirMatServicesUrl + RESOURCE_DEFINITION, ResourceDefinition[].class);
        } catch (RestClientResponseException e) {
            throw new CqlParseException(e);
        }
        return response.getBody() == null ? Collections.emptyList() : Arrays.asList(response.getBody());
    }
}
