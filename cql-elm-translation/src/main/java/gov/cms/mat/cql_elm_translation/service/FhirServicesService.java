package gov.cms.mat.cql_elm_translation.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
@Slf4j
public class FhirServicesService {
    private final RestTemplate restTemplate;
    private final MatXmlConversionService matXmlConversionService;

    @Value("${fhir.conversion.baseurl}")
    private String baseURL;

    public FhirServicesService(RestTemplate restTemplate, MatXmlConversionService matXmlConversionService) {
        this.restTemplate = restTemplate;
        this.matXmlConversionService = matXmlConversionService;
    }

    public String getCql(String name, String version, String qdmVersion) {
        URI uri = buildUri(name, version, qdmVersion);
        log.info("Getting library: {} ", uri);

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            if (responseEntity.hasBody()) {
                return matXmlConversionService.processCqlXml(responseEntity.getBody());
            } else {
                return null;
            }
        } else {
            return null;
        }
    }


    private URI buildUri(String name, String version, String qdmVersion) {
        return UriComponentsBuilder
                .fromHttpUrl(baseURL + "/library/find")
                .queryParam("qdmVersion", qdmVersion)
                .queryParam("name", name)
                .queryParam("version", version)
                .build()
                .encode()
                .toUri();
    }
}
