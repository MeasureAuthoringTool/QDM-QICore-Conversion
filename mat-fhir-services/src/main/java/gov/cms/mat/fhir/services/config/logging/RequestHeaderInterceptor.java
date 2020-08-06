package gov.cms.mat.fhir.services.config.logging;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class that get the header data from the request and set in the MDC context
 */
@Slf4j
public class RequestHeaderInterceptor extends HandlerInterceptorAdapter {
    public static final String XGEN2_START_KEY = "exec-start";
    public static final String XGEN2_PARAMS_ID = "mdc-params";

    private static final String HEADER_TEMPLATE = "%s:\"%s\"";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, /* not used */
                             Object object /* not used */) {
        request.setAttribute(XGEN2_START_KEY, System.currentTimeMillis());
        logRequest(request);

        String params = request.getHeader(XGEN2_PARAMS_ID);

        List<NameValuePair> nameValuePairs = parseParams(params);

        nameValuePairs.forEach(n -> MDC.put(n.getName(), n.getValue()));

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                @Nullable Exception ex) throws Exception {
        long executionTime = -1;

        Object attribute = request.getAttribute(XGEN2_START_KEY);
        if (attribute instanceof Long) {
            Long startTime = (Long) attribute;
            executionTime = System.currentTimeMillis() - startTime;
        }

        logResponse(response, executionTime);
    }

    List<NameValuePair> parseParams(String params) {
        if (StringUtils.isBlank(params)) {
            log.warn("Params string is blank");
            return Collections.emptyList();
        } else {
            String[] paramsArray = params.split(",");

            return Arrays.stream(paramsArray)
                    .filter(this::checkParam)
                    .map(this::parseParam)
                    .collect(Collectors.toList());
        }
    }

    private boolean checkParam(String param) {
        if (StringUtils.isBlank(param)) {
            log.warn("Param string is blank");
            return false;
        }

        int matches = StringUtils.countMatches(param, "=");

        if (matches == 1) {
            return true;
        } else {
            log.warn("Cannot parse param string: {}", param);
            return false;
        }
    }

    private NameValuePair parseParam(String param) {
        String[] paramsArray = param.split("=");

        return new BasicHeader(paramsArray[0].trim(), paramsArray[1].trim());
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

        return joinHeaders(headers);
    }

    private void logResponse(HttpServletResponse response, long executionTime) {
        if (log.isInfoEnabled()) {

            HttpStatus httpStatus = HttpStatus.resolve(response.getStatus());

            String statusText = httpStatus == null ? "" : httpStatus.getReasonPhrase();

            String builder = "\n" + "============================incoming response begin==========================================\n" +
                    "Status code   : " + response.getStatus() + " " + statusText + "\n" +
                    "Exec Time ms  : " + executionTime + "\n" +
                    "Headers       : " + processResponseHeaders(response) + "\n" +
                    "=======================incoming response end=================================================\n";
            log.info(builder);
        }
    }

    private String processResponseHeaders(HttpServletResponse response) {
        List<String> headers = response.getHeaderNames().stream()
                .map(name -> String.format(HEADER_TEMPLATE, name, response.getHeader(name)))
                .collect(Collectors.toList());

        return joinHeaders(headers);
    }

    private String joinHeaders(List<String> headers) {
        if (headers.isEmpty()) {
            return "";
        } else {
            return String.join(", ", headers);
        }
    }
}
