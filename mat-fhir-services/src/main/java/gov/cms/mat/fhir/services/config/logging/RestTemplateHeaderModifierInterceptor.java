package gov.cms.mat.fhir.services.config.logging;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static gov.cms.mat.fhir.services.config.logging.RequestHeaderInterceptor.XGEN2_PARAMS_ID;

@Slf4j
public class RestTemplateHeaderModifierInterceptor {


    public void processMDCHeader(HttpRequest request) {
        var optionalMDCParamString = createMDCParamString();

        if (optionalMDCParamString.isEmpty()) {
            log.warn("No MDC params");
        } else {
            String paramString = optionalMDCParamString.get();
            log.debug("MDCParamString: {}", paramString);
            request.getHeaders().add(XGEN2_PARAMS_ID, paramString);
        }
    }

    private Optional<String> createMDCParamString() {
        Map<String, String> mdcMap = MDC.getCopyOfContextMap();

        if (CollectionUtils.isEmpty(mdcMap)) {
            return Optional.empty();
        } else {
            return Optional.of(parseMap(mdcMap));
        }
    }

    private String parseMap(Map<String, String> mdcMap) {
        List<String> nameValuePairs =
                mdcMap.entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .collect(Collectors.toList());

        return String.join(" , ", nameValuePairs);
    }
}
