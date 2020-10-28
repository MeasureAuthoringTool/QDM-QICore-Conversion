package gov.cms.mat.fhir.services.config.logging;

import gov.cms.mat.config.logging.MdcPairParser;
import gov.cms.mat.config.logging.ServletLogging;
import gov.cms.mat.config.logging.ThreadLocalBody;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

import static gov.cms.mat.config.logging.MdcHeaderString.MDC_PARAMS_ID;
import static gov.cms.mat.config.logging.MdcHeaderString.MDC_START_KEY;

/**
 * Class that get the header data from the request and set in the MDC context
 */
@Slf4j
public class RequestHeaderInterceptor extends HandlerInterceptorAdapter {
    private static final String HEADER_TEMPLATE = "%s:\"%s\"";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, /* not used */
                             Object object /* not used */) {
        request.setAttribute(MDC_START_KEY, System.currentTimeMillis());
        String params = request.getHeader(MDC_PARAMS_ID);
        MdcPairParser.parseAndSetInMdc(params);
        logRequest(request);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                @Nullable Exception ex) throws Exception {
        long executionTime = -1;

        Object attribute = request.getAttribute(MDC_START_KEY);
        if (attribute instanceof Long) {
            Long startTime = (Long) attribute;
            executionTime = System.currentTimeMillis() - startTime;
        }

        logResponse(response, executionTime);
        MDC.clear();
    }

    public void logRequest(HttpServletRequest request) {

        if (log.isDebugEnabled()) {
            String body = "";

            try {
                body = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.debug("Cannot find body");
            }
            String headers = processRequestHeaders(request);

            ServletLogging.logIncomingRequest(request.getRequestURI(),
                    request.getQueryString(),
                    request.getMethod(),
                    headers,
                    body);

        }
    }

    private String processRequestHeaders(HttpServletRequest request) {
        List<String> headers = new ArrayList<>();

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            String value = request.getHeader(name);
            headers.add(String.format(HEADER_TEMPLATE, name, value));
        }

        return String.join(", ", headers);
    }

    private void logResponse(HttpServletResponse response, long executionTime) {
        if (log.isDebugEnabled()) {
            HttpStatus httpStatus = HttpStatus.resolve(response.getStatus());
            String statusText = httpStatus == null ? "" : httpStatus.getReasonPhrase();
            String status = response.getStatus() + " " + statusText;

            String headers = processResponseHeadersForLog(response);
            String body = ThreadLocalBody.getBody();

            ServletLogging.logIncomingResponse(status, executionTime, headers, body);
        }
    }

    private String processResponseHeadersForLog(HttpServletResponse response) {
        return response.getHeaderNames().stream()
                .map(name -> String.format(HEADER_TEMPLATE, name, response.getHeader(name)))
                .collect(Collectors.joining(", "));
    }
}
