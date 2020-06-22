package gov.cms.mat.fhir.services.components.vsac;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.fhir.services.config.VsacConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Reference Documentation: https://www.nlm.nih.gov/vsac/support/usingvsac/vsacsvsapiv2.html
 */
@Component
@Slf4j
public class VsacRestClient {
    private static final String CANNOT_OBTAIN_A_SINGLE_USE_SERVICE_TICKET = "Cannot obtain a single-use service ticket.";
    private static final String GRANTING_TICKET_REQUEST_TEMPLATE = "username=%s&password=%s";
    private static final String SINGLE_USE_TICKET_REQUEST = "service=http://umlsks.nlm.nih.gov";
    private static final String TICKET_PATH = "/vsac/ws/Ticket";
    private final RestTemplate restTemplate;
    private final VsacConfig vsacConfig;

    private final ObjectMapper mapper = new ObjectMapper();

    public VsacRestClient(RestTemplate restTemplate, VsacConfig vsacConfig) {
        this.restTemplate = restTemplate;
        this.vsacConfig = vsacConfig;
    }

    public String fetchGrantingTicket(String username, String password) {
        String postRequest = String.format(GRANTING_TICKET_REQUEST_TEMPLATE, username, password);
        return fetchTicket(postRequest, TICKET_PATH);
    }

    public String fetchSingleUseTicket(String grantingTicket) {
        String path = TICKET_PATH + '/' + grantingTicket;
        return fetchTicket(SINGLE_USE_TICKET_REQUEST, path);
    }

    public VsacResponse fetchCodeSystem(String path, String grantingTicket) {
        //  https://vsac.nlm.nih.gov/vsac/CodeSystem/LOINC/Version/2.66/Code/21112-8/Info?ticket=ST-281185-McNb53ZGHYtaGjHamgKg-cas&resultFormat=json&resultSet=standard
        // "/CodeSystem/LOINC22/Version/2.67/Code/21112-8/Info";

        String singleUseTicket = fetchSingleUseTicket(grantingTicket);

        if (StringUtils.isEmpty(singleUseTicket)) {
            return createErrorResponse(CANNOT_OBTAIN_A_SINGLE_USE_SERVICE_TICKET);
        }

        String url = vsacConfig.getService() + "/vsac" + path;

        URI uri = buildUri(singleUseTicket, url);

        log.debug("Vsac CodeSystem Uri: {}", uri);

        try {
            ResponseEntity<VsacResponse> response = restTemplate.getForEntity(uri, VsacResponse.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                if (response.hasBody()) {
                    return response.getBody();
                } else {
                    return createErrorResponse(response.getStatusCode().toString());
                }
            }
        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();

            try {
                return mapper.readValue(errorBody, VsacResponse.class);
            } catch (JsonProcessingException jsonProcessingException) {
                return createErrorResponse(e.getStatusText());
            }
        }
    }

    public VsacResponse createErrorResponse(String message) {
        VsacResponse vsacResponse = new VsacResponse();
        vsacResponse.setStatus("error");
        vsacResponse.setMessage(message);
        return vsacResponse;
    }

    private URI buildUri(String singleUseTicket, String url) {
        return UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("ticket", singleUseTicket)
                .queryParam("resultFormat", "json")
                .queryParam("resultSet", "standard")
                .build()
                .encode()
                .toUri();
    }

    private String fetchTicket(String postRequest, String path) {
        HttpEntity<String> request = new HttpEntity<>(postRequest);

        String url = vsacConfig.getService() + path;

        log.debug("Calling Vsac url: {}", url);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                return null;
            }
        } catch (HttpClientErrorException e) {
            log.info("Vsac Rest Error", e);
            return null;
        }
    }
}
