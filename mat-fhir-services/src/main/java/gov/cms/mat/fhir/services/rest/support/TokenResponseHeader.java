package gov.cms.mat.fhir.services.rest.support;

import gov.cms.mat.vsac.RefreshTokenManagerImpl;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletResponse;

public interface TokenResponseHeader {
    default void processResponseHeader(HttpServletResponse response) {
        String token = RefreshTokenManagerImpl.getInstance().getRefreshedToken();

        if (StringUtils.isNotBlank(token)) {
            response.setHeader("Refreshed-Granting-Ticket", token);
        }
    }
}
