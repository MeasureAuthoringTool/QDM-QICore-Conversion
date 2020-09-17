package gov.cms.mat.fhir.services.components.vsac;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.fhir.services.config.VsacConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Reference Documentation: https://www.nlm.nih.gov/vsac/support/usingvsac/vsacsvsapiv2.html
 */
@Component
@Slf4j
public class VsacRestClient {
    static final String PROFILE = "Most Recent Code System Versions in VSAC";
    private static final String CANNOT_OBTAIN_A_SINGLE_USE_SERVICE_TICKET = "Cannot obtain a single-use service ticket.";
    private static final String GRANTING_TICKET_REQUEST_TEMPLATE = "username=%s&password=%s";
    private static final String SINGLE_USE_TICKET_REQUEST = "service=http://umlsks.nlm.nih.gov";
    private static final String TICKET_PATH = "/vsac/ws/Ticket";
    @Qualifier("externalRestTemplate")
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
        grantingTicket = "TGT-310648-jTl0DTuveHIWbkmO90IKJWc4d1n46RrCHTq5zlPp5dmg1rCNAA-cas";

        String path = TICKET_PATH + '/' + grantingTicket;
        return fetchTicket(SINGLE_USE_TICKET_REQUEST, path);
    }

    public ValueSetVSACResponseResult getDataFromProfile(String oid, String grantingTicket) {

        String singleUseTicket = fetchSingleUseTicket(grantingTicket);

        if (StringUtils.isEmpty(singleUseTicket)) {
            return ValueSetVSACResponseResult.builder()
                    .isFailResponse(true)
                    .failReason(CANNOT_OBTAIN_A_SINGLE_USE_SERVICE_TICKET)
                    .build();
        }

        String url = vsacConfig.getService() + "/vsac/svs/RetrieveMultipleValueSets";

        URI uri = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("ticket", singleUseTicket)
                .queryParam("id", oid)
                .queryParam("profile", PROFILE)
                .build()
                .encode()
                .toUri();

        try {
            String xml = restTemplate.getForObject(uri, String.class);

            return ValueSetVSACResponseResult.builder()
                    .isFailResponse(false)
                    .xmlPayLoad(xml)
                    .build();
        } catch (RestClientException e) {
            String message;
            if(Objects.equals(e.getMessage(), "404 : [no body]")) {
               message = "404 Cannot find value set with oid: " + oid;
            } else  {
                message = e.getMessage();
            }

            return ValueSetVSACResponseResult.builder()
                    .isFailResponse(true)
                    .failReason(message)
                    .build();
        }
    }

    @Cacheable(value = "vsacVersions", key = "#name")
    public CodeSystemVersionResponse fetchVersionFromName(String name, String grantingTicket) {
        String path = "/CodeSystem/" + name + "/Info";

        VsacResponse vsacResponse = fetchCodeSystem(path, grantingTicket);

        if (vsacResponse.getMessage().equals("ok") && vsacResponse.getData() != null
                && vsacResponse.getData().getResultSet().size() == 1 &&
                vsacResponse.getData().getResultSet().get(0).getCsVersion() != null) {

            return CodeSystemVersionResponse.builder()
                    .message("ok")
                    .success(Boolean.TRUE)
                    .version(vsacResponse.getData().getResultSet().get(0).getCsVersion())
                    .build();
        } else {
            String errorMessage;

            if (vsacResponse.getErrors() == null || CollectionUtils.isEmpty(vsacResponse.getErrors().getResultSet())) {
                if (StringUtils.isEmpty(vsacResponse.getMessage())) {
                    errorMessage = "Unknown Error obtaining version from VSAC"; // should never happen
                } else {
                    errorMessage = vsacResponse.getMessage();
                }
            } else {
                List<String> strList = vsacResponse.getErrors().getResultSet()
                        .stream()
                        .map(VsacResponse.VsacErrorResultSet::getErrDesc)
                        .collect(Collectors.toList());

                errorMessage = String.join(", ", strList);
            }

            return CodeSystemVersionResponse.builder()
                    .message(errorMessage)
                    .success(Boolean.FALSE)
                    .build();
        }
    }

    // cannot cache due to all users not having the same rights
    public VsacResponse fetchCodeSystem(String path, String grantingTicket) {
        //  https://vsac.nlm.nih.gov/vsac/CodeSystem/LOINC/Version/2.66/Code/21112-8/Info?ticket=ST-281185-McNb53ZGHYtaGjHamgKg-cas&resultFormat=json&resultSet=standard
        // "/CodeSystem/LOINC22/Version/2.67/Code/21112-8/Info";

        String singleUseTicket = fetchSingleUseTicket(grantingTicket);

        if (StringUtils.isEmpty(singleUseTicket)) {
            return createErrorResponse(CANNOT_OBTAIN_A_SINGLE_USE_SERVICE_TICKET);
        }

        String url = vsacConfig.getService() + "/vsac" + path;

        URI uri = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("ticket", singleUseTicket)
                .queryParam("resultFormat", "json")
                .queryParam("resultSet", "standard")
                .build()
                .encode()
                .toUri();

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
        } catch (Exception e) {
            log.info("Vsac Rest Error", e);
            return null;
        }
    }

    @Builder
    @Getter
    public static class CodeSystemVersionResponse {
        private final String version;
        private final Boolean success;
        private final String message;
    }
}
