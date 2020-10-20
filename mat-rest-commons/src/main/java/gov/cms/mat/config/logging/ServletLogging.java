package gov.cms.mat.config.logging;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;

@Slf4j
public final class ServletLogging {
    private static final int MAX_BODY_TO_LOG = 4096;

    private ServletLogging() {
    }

    public static void logIncomingRequest(String requestURI, String queryString, String method, String headers, String body) {
        if (log.isDebugEnabled()) {
            log.debug("Incoming Request Uri: {}", requestURI);

            if (StringUtils.isNotBlank(queryString)) {
                log.debug("QueryString: {}", queryString);
            }

            log.debug("Method: {}", method);
            log.debug("Headers: {}", headers);

            if (StringUtils.isNotBlank(body) && body.length() <= MAX_BODY_TO_LOG) {
                log.debug("Request body : {}", body);
            }
        }
    }

    public static void logIncomingResponse(String status, long executionTime, String headers, String body) {
        if (log.isDebugEnabled()) {
            log.debug("Incoming Response Status: {}", status);
            log.debug("Exec Time ms: {}", executionTime);
            log.debug("Headers: {}", headers);

            if (StringUtils.isNotBlank(body) && body.length() <= MAX_BODY_TO_LOG) {
                log.debug("Response body: {}", body);
            }
        }
    }

    public static void logOutgoingRequest(URI requestURI, String method, String headers, String body) {
        if (log.isDebugEnabled()) {
            log.debug("Outgoing Request Uri: {}", requestURI);
            log.debug("Method: {}", method);
            log.debug("Headers: {}", headers);

            if (StringUtils.isNotBlank(body) && body.length() <= MAX_BODY_TO_LOG) {
                log.debug("Request body : {}", body);
            }
        }
    }

    public static void logOutgoingResponse(String statusCode, String statusText, long executionTime, String headers, String body) {
        if (log.isDebugEnabled()) {
            log.debug("Outgoing Response Status: {}", statusCode);

            if (StringUtils.isNotBlank(statusText)) {
                log.debug("Status Text: {}", statusText);
            }

            log.debug("Exec Time ms: {}", executionTime);
            log.debug("Headers: {}", headers);

            if (StringUtils.isNotBlank(body) && body.length() <= MAX_BODY_TO_LOG) {
                log.debug("Response body: {}", body);
            }
        }
    }
}
