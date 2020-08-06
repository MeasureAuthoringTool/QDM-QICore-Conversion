package gov.cms.mat.fhir.services.config.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Slf4j
@ControllerAdvice
public class HeaderResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        logResponse(response, body);
//        String start = MDC.get(XGEN2_START_KEY);
//
//        int totalTime = -1;
//        String requestUri = "";
//
//
//        if (request instanceof ServletServerHttpRequest) {
//            requestUri = ((ServletServerHttpRequest) request).getServletRequest().getRequestURI();
//        }
//
//        if (StringUtils.isNotBlank(start) && StringUtils.isNotBlank(requestUri)) {
//            totalTime = (int) (System.currentTimeMillis() - Long.parseLong(start));
//
//            log.info("Total execution time for {}:{} is {} ms", request.getMethod(), requestUri, totalTime);
//        } else {
//            log.error("Cannot set controller execution time for {}:{}, missing start time",
//                    request.getMethod(),
//                    StringUtils.isNotBlank(requestUri) ? requestUri : "unknown");
//        }


        return body;
    }

    private void logResponse(ServerHttpResponse response, Object body)  {
        if (log.isInfoEnabled()) {

            String builder = "\n" + "==========================incoming response-body begin==========================================\n" +
                    "Response body : " + (body == null ? "null" : body) + "\n" +
                    "=======================incoming response-body end=================================================\n";
            log.info(builder);
        }
    }
}