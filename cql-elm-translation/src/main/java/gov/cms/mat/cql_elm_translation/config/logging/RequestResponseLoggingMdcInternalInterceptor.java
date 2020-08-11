package gov.cms.mat.cql_elm_translation.config.logging;

import gov.cms.mat.config.logging.MdcHeaderString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;

import static gov.cms.mat.config.logging.MdcHeaderString.MDC_PARAMS_ID;

@Slf4j
public class RequestResponseLoggingMdcInternalInterceptor extends RequestResponseLoggingInterceptor {
    @Override
    protected void processHeaders(HttpRequest request) {
        var optionalMDCParamString = MdcHeaderString.create();

        if (optionalMDCParamString.isEmpty()) {
            log.warn("No MDC params");
        } else {
            String paramString = optionalMDCParamString.get();
            log.debug("MDCParamString: {}", paramString);
            request.getHeaders().add(MDC_PARAMS_ID, paramString);
        }
    }
}

