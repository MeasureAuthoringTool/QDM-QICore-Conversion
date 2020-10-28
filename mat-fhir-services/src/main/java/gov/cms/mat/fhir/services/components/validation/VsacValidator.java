package gov.cms.mat.fhir.services.components.validation;

import gov.cms.mat.vsac.VsacService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class VsacValidator {
    static final String EXPIRED_TICKET = "VSAC ticket has expired";

    final VsacService vsacService;

    public VsacValidator(VsacService vsacService) {
        this.vsacService = vsacService;
    }

    String fetchFiveMinuteTicket(String umlsToken) {
        String fiveMinServiceTicket = vsacService.getServiceTicket(umlsToken);

        if (StringUtils.isBlank(fiveMinServiceTicket)) {
            throw new ExpiredTicketException(EXPIRED_TICKET);
        } else {
            return fiveMinServiceTicket;
        }

    }

    static class ExpiredTicketException extends RuntimeException {
        public ExpiredTicketException(String message) {
            super(message);
            log.debug("ExpiredTicketException: {} ", message);
        }
    }

    static class VsacCodeSystemValidatorException extends RuntimeException {
        public VsacCodeSystemValidatorException(String message) {
            super(message);
            log.debug("VsacCodeSystemValidatorException: {} ", message);
        }
    }
}
