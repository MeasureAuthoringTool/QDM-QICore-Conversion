package gov.cms.mat.fhir.services.config.logging;

import org.slf4j.Logger;
import org.springframework.http.HttpRequest;

import java.nio.charset.StandardCharsets;

public final class MdcUtil {

    public static void logRequest(Logger log, HttpRequest request, String body) {
        if (log.isInfoEnabled()) {
            String builder = "\n" + "===========================request begin================================================\n" +
                    "URI          : " + request.getURI() + "\n" +
                    "Method       : " + request.getMethod() + "\n" +
                    "Headers      : " + request.getHeaders() + "\n" +
                    "Request body : " + body + "\n" +
                    "==========================request end================================================\n";
            log.info(builder);
        }
    }
}
