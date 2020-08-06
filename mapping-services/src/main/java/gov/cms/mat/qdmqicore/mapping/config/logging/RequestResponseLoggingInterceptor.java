package gov.cms.mat.qdmqicore.mapping.config.logging;

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
public abstract class RequestResponseLoggingInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request,
                                        byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        long start = System.currentTimeMillis();
        processHeaders(request);
        logRequest(request, new String(body, StandardCharsets.UTF_8));

        ClientHttpResponse response = execution.execute(request, body);

        logResponse(response, start);
        return response;
    }

    protected abstract void processHeaders(HttpRequest request);


    private void logResponse(ClientHttpResponse response, long start) throws IOException {
        if (log.isInfoEnabled()) {
            long executionTime = System.currentTimeMillis() - start;

            String builder = "\n" + "============================response begin==========================================\n" +
                    "Status code   : " + response.getStatusCode() + "\n" +
                    "Status text   : " + response.getStatusText() + "\n" +
                    "Exec Time ms  : " + executionTime + "\n" +
                    "Headers       : " + response.getHeaders() + "\n" +
                    "Response body : " + StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()) + "\n" +
                    "=======================response end=================================================\n";
            log.info(builder);
        }
    }

    private void logRequest(HttpRequest request, String body) {
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