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

    public ResponseEntity<String> getData(String xml) {
        RequestEntity<String> requestEntity = buildRequestEntity(xml);
        log.debug("requestEntity: {}", requestEntity.getUrl());

        return restTemplate.exchange(requestEntity, String.class);
    }

    public RequestEntity<String> buildRequestEntity(String xml) {
        return RequestEntity
                .put(buildUri())
                //.accept(MediaType.TEXT_PLAIN)
                .body(xml);
    }

    private URI buildUri() {
        return UriComponentsBuilder
                .fromHttpUrl(baseURL + "/cql/translator/xml")
                .build()
                .encode()
                .toUri();
    }
}
