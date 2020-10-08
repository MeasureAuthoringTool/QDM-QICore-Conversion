package gov.cms.mat.patients.conversion.config.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class SecurityFilter implements Filter {
    public static final String MAT_API_KEY = "MAT-API-KEY";
    private static final String DISABLED = "DISABLED";
    @Value("${mat-api-key}")
    private String matApiKey;

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;

        if (!StringUtils.equals(matApiKey, DISABLED) && !isWhiteListUrl(req)) {
            String keyValue = req.getHeader(MAT_API_KEY);
            if (keyValue == null) {
                log.error("Request did not contain header " + MAT_API_KEY);
                res.sendError(403);
            } else if (!StringUtils.equals(matApiKey, keyValue)) {
                log.error("Invalid " + MAT_API_KEY + " header.");
                res.sendError(403);
            } else {
                log.info("Request contained valid " + MAT_API_KEY);
                filterChain.doFilter(servletRequest, servletResponse);
            }
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private boolean isWhiteListUrl(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String method = req.getMethod();
        return uri.equals("/actuator/health") && method.equals("GET");
    }
}
