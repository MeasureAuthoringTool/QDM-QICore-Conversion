package gov.cms.mat.fhir.services.components.validation;

import gov.cms.mat.fhir.services.service.VsacService;
import lombok.extern.slf4j.Slf4j;
import mat.model.cql.CQLCode;
import mat.model.cql.VsacStatus;
import mat.shared.CQLModelValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.vsac.VSACResponseResult;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
class CodeSystemVsacAsync extends VsacValidator {
    private static final String INVALID_CODE_URL = "Invalid CODE URL";
    private static final String URL_IS_REQUIRED = "URL is required";

    CodeSystemVsacAsync(VsacService vsacService) {
        super(vsacService);
    }

    @Async("codeSystemTheadPoolValidation")
    CompletableFuture<Void> validateCode(CQLCode cqlCode, String umlsToken) {

        if (StringUtils.contains(cqlCode.getCodeSystemOID(), "NOT.IN.VSAC")) {
            log.debug("No need to process NOT.IN.VSAC cqlCode: {}", cqlCode.getCodeSystemName());
        } else {
            try {
                boolean isValid = isDirectReferenceCodeValid(cqlCode.getCodeIdentifier(), umlsToken);
                log.info("Validated code {} with vsac. {}", cqlCode.getCodeIdentifier(), isValid);

                cqlCode.setErrorMessage(isValid ? null : NOT_IN_VSAC);
                cqlCode.addValidatedWithVsac(isValid ? VsacStatus.VALID : VsacStatus.IN_VALID);
            } catch (Exception e) {
                cqlCode.addValidatedWithVsac(VsacStatus.IN_VALID);
                cqlCode.setErrorMessage(e.getMessage());
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    private boolean isDirectReferenceCodeValid(String url, String umlsToken) {

        if (StringUtils.isBlank(url)) {
            throw new VsacCodeSystemValidatorException(URL_IS_REQUIRED);
        }

        CQLModelValidator validator = new CQLModelValidator();

        if (validator.validateForCodeIdentifier(url)) {
            throw new VsacCodeSystemValidatorException(INVALID_CODE_URL);
        }

        String fiveMinServiceTicket = fetchFiveMinuteTicket(umlsToken);

        if (url.contains(":")) {
            String[] arg = url.split(":");
            if (arg.length > 0 && arg[1] != null) {
                url = arg[1];
            }
        }

        VSACResponseResult vsacResponseResult = vsacService.getDirectReferenceCode(url, fiveMinServiceTicket);

        return vsacResponseResult != null && vsacResponseResult.getXmlPayLoad() != null && !StringUtils.isEmpty(vsacResponseResult.getXmlPayLoad());
    }

}
