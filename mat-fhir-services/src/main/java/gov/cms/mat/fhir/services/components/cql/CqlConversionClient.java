package gov.cms.mat.fhir.services.components.cql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
@Slf4j
public class CqlConversionClient {
    private final RestTemplate restTemplate;
    @Value("${cql.conversion.baseurl}")
    private String baseURL;

    public CqlConversionClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<String> getJson(String cql) {
        RequestEntity<String> requestEntity = buildConvertCqlToJsonRequestEntity(cql);
        log.trace("PUT-JSON requestEntity: {}", requestEntity.getUrl());

        return restTemplate.exchange(requestEntity, String.class);
    }

    public ResponseEntity<String> getCql(String cqlXml) {
        RequestEntity<String> requestEntity = buildConvertXmlRequestEntity(cqlXml);
        log.debug("PUT-CQL requestEntity: {}", requestEntity.getUrl());

        return restTemplate.exchange(requestEntity, String.class);
    }

    private RequestEntity<String> buildConvertCqlToJsonRequestEntity(String xml) {
        return RequestEntity
                .put(buildUriJson())
                .body(xml);
    }

    private URI buildUriJson() {
        return UriComponentsBuilder
                .fromHttpUrl(baseURL + "/cql/translator/cql")
                .build()
                .encode()
                .toUri();
    }

    private URI buildUriCql() {
        return UriComponentsBuilder
                .fromHttpUrl(baseURL + "/cql/marshaller")
                .build()
                .encode()
                .toUri();
    }

    public RequestEntity<String> buildConvertXmlRequestEntity(String cqlXml) {
        return RequestEntity
                .put(buildUriCql())
                .body(cqlXml);
    }
}
