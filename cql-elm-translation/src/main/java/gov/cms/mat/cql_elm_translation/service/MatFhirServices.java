package gov.cms.mat.cql_elm_translation.service;

import gov.cms.mat.fhir.rest.dto.cql.CqlPayload;
import gov.cms.mat.fhir.rest.dto.cql.CqlPayloadType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
@Slf4j
public class MatFhirServices {
    private final RestTemplate restTemplate;
    private final MatXmlConversionService matXmlConversionService;

    @Value("${fhir.conversion.baseurl}")
    private String baseURL;

    public MatFhirServices(RestTemplate restTemplate, MatXmlConversionService matXmlConversionService) {
        this.restTemplate = restTemplate;
        this.matXmlConversionService = matXmlConversionService;
    }

    public String getMatCql(String name, String version, String qdmVersion, String libraryType) {
        URI uri = buildFindMatUri(name, version, qdmVersion, libraryType);
        log.info("Getting Mat library: {} ", uri);

        ResponseEntity<CqlPayload> responseEntity = restTemplate.getForEntity(uri, CqlPayload.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            if (responseEntity.hasBody()) {
                CqlPayload cqlPayload = responseEntity.getBody();

                if (cqlPayload == null || cqlPayload.getType() == null) {
                    log.error("cqlPayload is invalid");
                    return null;
                } else if (cqlPayload.getType() == CqlPayloadType.XML) {
                    return matXmlConversionService.processCqlXml(cqlPayload.getData());
                } else {
                    return cqlPayload.getData();
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public String getFhirCql(String name, String version) {
        URI uri = buildFindFhirUri(name, version);
        log.info("Getting Fhir library: {} ", uri);

        ResponseEntity<String> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(uri, String.class);
        } catch (Exception e) {
            return null;
        }

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            if (responseEntity.hasBody()) {
                return responseEntity.getBody();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }


    private URI buildFindMatUri(String name, String version, String qdmVersion, String type) {
        return UriComponentsBuilder
                .fromHttpUrl(baseURL + "/library/find/mat")
                .queryParam("qdmVersion", qdmVersion)
                .queryParam("name", name)
                .queryParam("version", version)
                .queryParam("type", type)
                .build()
                .encode()
                .toUri();
    }

    private URI buildFindFhirUri(String name, String version) {
        return UriComponentsBuilder
                .fromHttpUrl(baseURL + "/library/find/hapi")
                .queryParam("name", name)
                .queryParam("version", version)
                .build()
                .encode()
                .toUri();
    }
}
