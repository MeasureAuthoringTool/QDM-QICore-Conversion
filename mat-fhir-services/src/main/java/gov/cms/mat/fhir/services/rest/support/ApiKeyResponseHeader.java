package gov.cms.mat.fhir.services.rest.support;

import gov.cms.mat.vsac.ApiKeyManagerImpl;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletResponse;

public interface ApiKeyResponseHeader {
    default void processResponseHeader(HttpServletResponse response) {
    	String apiKey = ApiKeyManagerImpl.getInstance().getApiKey();

        if (StringUtils.isNotBlank(apiKey)) {
            response.setHeader("API-KEY", apiKey);
        }
    }
}
