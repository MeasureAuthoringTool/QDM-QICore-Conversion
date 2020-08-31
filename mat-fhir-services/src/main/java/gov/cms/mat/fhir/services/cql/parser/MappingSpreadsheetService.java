package gov.cms.mat.fhir.services.cql.parser;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gov.cms.mat.fhir.rest.dto.spreadsheet.ConversionAttributes;
import gov.cms.mat.fhir.rest.dto.spreadsheet.ConversionDataTypes;
import gov.cms.mat.fhir.services.exceptions.CqlParseException;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MappingSpreadsheetService {
    private static final String RESOURCE_DEFINITION = "/resourceDefinition";
    private static final String CONVERSION_DATA_TYPES = "/conversionDataTypes";
    private static final String CONVERSION_ATTRIBUTES = "/conversionAttributes";

    private final RestTemplate restTemplate;


    @Value("${mapping.services.baseurl}")
    private String conversionUrl;

    public MappingSpreadsheetService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Cacheable("spreadSheetfhirTypes")
    public List<String> getFhirTypes() {
        return resourceDefinitions().stream().
                filter(r -> StringUtils.isNotBlank(r.getElementId()) &&
                        StringUtils.contains(r.getElementId(), '.')).
                map(r -> r.getElementId().substring(0, r.getElementId().lastIndexOf("."))).
                distinct().
                sorted(String.CASE_INSENSITIVE_ORDER).
                collect(Collectors.toList());
    }

    @Cacheable("spreadSheetResourceDefinitions")
    public List<ResourceDefinition> resourceDefinitions() {
        try {
            ResponseEntity<ResourceDefinition[]> response =
                    restTemplate.getForEntity(conversionUrl + RESOURCE_DEFINITION, ResourceDefinition[].class);
            return response.getBody() == null ? Collections.emptyList() : Arrays.asList(response.getBody());
        } catch (RestClientResponseException e) {
            throw new CqlParseException(e);
        }
    }

    @Cacheable("conversionDataTypes")
    public List<ConversionDataTypes> fetchConversionDataTypes() {
        try {
            ResponseEntity<ConversionDataTypes[]> response =
                    restTemplate.getForEntity(conversionUrl + CONVERSION_DATA_TYPES, ConversionDataTypes[].class);
            return response.getBody() == null ? Collections.emptyList() : Arrays.asList(response.getBody());
        } catch (RestClientResponseException e) {
            throw new CqlParseException(e);
        }
    }

    @Cacheable("ConversionAttributes")
    public List<ConversionAttributes> fetchConversionAttributes() {
        try {
            ResponseEntity<ConversionAttributes[]> response =
                    restTemplate.getForEntity(conversionUrl + CONVERSION_ATTRIBUTES, ConversionAttributes[].class);
            return response.getBody() == null ? Collections.emptyList() : Arrays.asList(response.getBody());
        } catch (RestClientResponseException e) {
            throw new CqlParseException(e);
        }
    }

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
}
