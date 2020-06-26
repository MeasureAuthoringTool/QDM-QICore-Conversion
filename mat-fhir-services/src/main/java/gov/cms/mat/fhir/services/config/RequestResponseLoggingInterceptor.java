package gov.cms.mat.fhir.services.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
public class RequestResponseLoggingInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request,
                                        byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        logRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        logResponse(response);
        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) {
        if (log.isInfoEnabled()) {
            String builder = "\n" + "===========================request begin================================================\n" +
                    "URI          : " + request.getURI() + "\n" +
                    "Method       : " + request.getMethod() + "\n" +
                    "Headers      : " + request.getHeaders() + "\n" +
                    "Request body : " + new String(body, StandardCharsets.UTF_8) + "\n" +
                    "==========================request end================================================\n";
            log.info(builder);
        }
    }

    private void logResponse(ClientHttpResponse response) throws IOException {
        if (log.isInfoEnabled()) {
            String builder = "\n" + "============================response begin==========================================\n" +
                    "Status code   : " + response.getStatusCode() + "\n" +
                    "Status text   : " + response.getStatusText() + "\n" +
                    "Headers       : " + response.getHeaders() + "\n" +
                    "Response body : " + StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()) + "\n" +
                    "=======================response end=================================================\n";
            log.info(builder);
        }
    }
}