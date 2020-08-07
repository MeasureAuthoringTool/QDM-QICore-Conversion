package gov.cms.mat.config.logging;

import lombok.extern.slf4j.Slf4j;

import java.net.URI;

@Slf4j
public final class ServletLogging {

    public static void logIncomingRequest(String requestURI, String queryString, String method, String headers, String body) {
        log.info("Incoming Request Uri: {}", requestURI);
        log.info("QueryString: {}", queryString);
        log.info("Method: {}", method);
        log.info("Headers: {}", headers);
        log.info("Request body : {}", body);
    }

    public static void logIncomingResponse(String status, long executionTime, String headers, String body) {
        log.info("Incoming Response Status: {}", status);
        log.info("Exec Time ms: {}", executionTime);
        log.info("Headers: {}", headers);
        log.info("Response body: {}", body);
    }

    public static void logOutgoingRequest(URI requestURI, String  method, String headers, String body) {
        log.info("Outgoing Request Uri: {}", requestURI);
        log.info("Method: {}", method);
        log.info("Headers: {}", headers);
        log.info("Request body : {}", body);
    }

    public static void logOutgoingResponse(String statusCode, String statusText, long executionTime, String headers, String body) {
        log.info("Outgoing Response Status: {}", statusCode);
        log.info("Status Text: {}", statusText);
        log.info("Exec Time ms: {}", executionTime);
        log.info("Headers: {}", headers);
        log.info("Response body: {}", body);
    }

}
