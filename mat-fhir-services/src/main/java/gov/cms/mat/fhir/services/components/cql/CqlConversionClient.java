package gov.cms.mat.fhir.services.components.cql;

import gov.cms.mat.cql.dto.CqlConversionPayload;
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

    public ResponseEntity<CqlConversionPayload> getJson(String cql, boolean showWarnings) {
        RequestEntity<String> requestEntity = buildConvertCqlToJsonRequestEntity(cql, showWarnings);
        log.trace("PUT-JSON requestEntity: {}", requestEntity.getUrl());

        return restTemplate.exchange(requestEntity, CqlConversionPayload.class);
    }

    public ResponseEntity<String> getCql(String cqlXml, boolean showWarnings) {
        RequestEntity<String> requestEntity = buildConvertXmlRequestEntity(cqlXml, showWarnings);
        log.debug("PUT-CQL requestEntity: {}", requestEntity.getUrl());

        return restTemplate.exchange(requestEntity, String.class);
    }

    private RequestEntity<String> buildConvertCqlToJsonRequestEntity(String xml, boolean showWarnings) {
        return RequestEntity
                .put(buildUriJson(showWarnings))
                .body(xml);
    }

    private URI buildUriJson(boolean showWarnings) {
        return UriComponentsBuilder
                .fromHttpUrl(baseURL + "/cql/translator/cql")
                .queryParam("showWarnings", showWarnings)
                .build()
                .encode()
                .toUri();
    }

    private URI buildUriCql(boolean showWarnings) {
        return UriComponentsBuilder
                .fromHttpUrl(baseURL + "/cql/marshaller")
                .queryParam("showWarnings", showWarnings)
                .build()
                .encode()
                .toUri();
    }

    public RequestEntity<String> buildConvertXmlRequestEntity(String cqlXml, boolean showWarnings) {
        return RequestEntity
                .put(buildUriCql(showWarnings))
                .body(cqlXml);
    }
}
