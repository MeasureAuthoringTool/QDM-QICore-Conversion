package gov.cms.mat.fhir.services.components.validation;

import gov.cms.mat.fhir.services.service.VsacService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ForkJoinPool;

@Slf4j
public class VsacValidator {
    static final String BLANK_UMLS_TOKEN = "UMLS token is blank";
    static final String EXPIRED_TICKET = "VSAC ticket has expired";
    static final String NOT_IN_VSAC = "Not In Vsac";

    final VsacService vsacService;

    public VsacValidator(VsacService vsacService) {
        this.vsacService = vsacService;
    }

    String fetchFiveMinuteTicket(String umlsToken) {
        String fiveMinServiceTicket = vsacService.getServiceTicket(umlsToken);

        if (StringUtils.isBlank(fiveMinServiceTicket)) {
            throw new VsacCodeSystemValidatorException(EXPIRED_TICKET);
        } else {
            return fiveMinServiceTicket;
        }

    }

    static class VsacCodeSystemValidatorException extends RuntimeException {
        public VsacCodeSystemValidatorException(String message) {
            super(message);
            log.debug("VsacCodeSystemValidatorException: {} ", message);
        }
    }
}
