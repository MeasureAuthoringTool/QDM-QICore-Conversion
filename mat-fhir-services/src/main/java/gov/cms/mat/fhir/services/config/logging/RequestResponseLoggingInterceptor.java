package gov.cms.mat.fhir.services.config.logging;

import gov.cms.mat.fhir.services.config.logging.MdcUtil;
import gov.cms.mat.fhir.services.config.logging.RestTemplateHeaderModifierInterceptor;
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
        long start = System.currentTimeMillis();
        processHeaders(request);
        MdcUtil.logRequest(log, request, new String(body, StandardCharsets.UTF_8));

        ClientHttpResponse response = execution.execute(request, body);

        logResponse(response, start);
        return response;
    }

    private void processHeaders(HttpRequest request) {
        RestTemplateHeaderModifierInterceptor interceptor = new RestTemplateHeaderModifierInterceptor();
        interceptor.processMDCHeader(request);
    }

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
}