package gov.cms.mat.cql_elm_translation.config.logging;

import gov.cms.mat.config.logging.MdcPairParser;
import gov.cms.mat.config.logging.ThreadLocalBody;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
        logRequest(request);
        String params = request.getHeader(MDC_PARAMS_ID);

        MdcPairParser.parseAndSetInMdc(params);

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
    }

    public void logRequest(HttpServletRequest request) {
        if (log.isInfoEnabled()) {
            String body = "";

            try {
                body = IOUtils.toString(request.getReader());
            } catch (IOException e) {
                log.error("Cannot find body", e);
            }

            String builder = "\n" + "=======================incoming request begin=============================================\n" +
                    "URI          : " + request.getRequestURI() + "\n" +
                    "QueryString  : " + request.getQueryString() + "\n" +
                    "Method       : " + request.getMethod() + "\n" +
                    "Headers      : " + processRequestHeaders(request) + "\n" +
                    "Request body : " + body + "\n" +
                    "==========================incoming request end================================================\n";
            log.info(builder);
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
        if (log.isInfoEnabled()) {

            HttpStatus httpStatus = HttpStatus.resolve(response.getStatus());

            String statusText = httpStatus == null ? "" : httpStatus.getReasonPhrase();

            String body = ThreadLocalBody.getBody();

            String builder = "\n" + "============================incoming response begin==========================================\n" +
                    "Status code   : " + response.getStatus() + " " + statusText + "\n" +
                    "Exec Time ms  : " + executionTime + "\n" +
                    "Headers       : " + processResponseHeadersForLog(response) + "\n" +
                    "Response body : " + body + "\n" +
                    "=======================incoming response end=================================================\n";

            log.info(builder);
        }
    }

    private String processResponseHeadersForLog(HttpServletResponse response) {
        return response.getHeaderNames().stream()
                .map(name -> String.format(HEADER_TEMPLATE, name, response.getHeader(name)))
                .collect(Collectors.joining(", "));
    }
}
