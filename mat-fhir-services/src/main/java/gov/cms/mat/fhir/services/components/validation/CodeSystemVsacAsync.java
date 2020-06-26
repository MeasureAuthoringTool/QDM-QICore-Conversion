package gov.cms.mat.fhir.services.components.validation;

import gov.cms.mat.fhir.services.components.vsac.VsacResponse;
import gov.cms.mat.fhir.services.components.vsac.VsacRestClient;
import gov.cms.mat.fhir.services.service.VsacService;
import lombok.extern.slf4j.Slf4j;
import mat.model.cql.CQLCode;
import mat.model.cql.VsacStatus;
import mat.shared.CQLModelValidator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
class CodeSystemVsacAsync extends VsacValidator {

    private static final String INVALID_CODE_URL = "Invalid code system uri.";
    private static final String URL_IS_REQUIRED = "Error processing with vsac.";

    public static final String REQUIRES_VALIDATION = "Code system requires validation. Please login to UMLS to validate it.";
    public static final String NOT_FOUND = "Code system not found in VSAC.";

    private final VsacRestClient vsacRestClient;

    CodeSystemVsacAsync(VsacService vsacService, VsacRestClient vsacRestClient) {
        super(vsacService);
        this.vsacRestClient = vsacRestClient;
    }

    @Async("codeSystemTheadPoolValidation")
    CompletableFuture<Void> validateCode(CQLCode cqlCode, String umlsToken) {
        if (StringUtils.contains(cqlCode.getCodeSystemOID(), "NOT.IN.VSAC")) {
            log.debug("No need to process NOT.IN.VSAC cqlCode: {}", cqlCode.getCodeSystemName());
        } else {
            try {
                isDirectReferenceCodeValid(cqlCode, umlsToken);
                log.info("Validated code {} with vsac {} message: {}",
                        cqlCode.getCodeIdentifier(),
                        cqlCode.isValidatedWithVsac(),
                        cqlCode.getErrorMessage());

            } catch (VsacCodeSystemValidatorException vc) {
                cqlCode.setErrorMessage(vc.getMessage());
            } catch (Exception e) {
                cqlCode.setErrorMessage(cqlCode.obtainValidatedWithVsac() == VsacStatus.PENDING ?
                        REQUIRES_VALIDATION : NOT_FOUND);
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    private void isDirectReferenceCodeValid(CQLCode cqlCode, String umlsToken) {

        String url = cqlCode.getCodeIdentifier();

        if (StringUtils.isBlank(url)) {
             throw new VsacCodeSystemValidatorException(URL_IS_REQUIRED);
        }

        CQLModelValidator validator = new CQLModelValidator();

        if (validator.validateForCodeIdentifier(url)) {
            throw new VsacCodeSystemValidatorException(INVALID_CODE_URL);
        }

        if (url.contains(":")) {
            String[] arg = url.split(":");
            if (arg.length > 0 && arg[1] != null) {
                url = arg[1];
            }
        }

        VsacResponse vsacResponse = vsacRestClient.fetchCodeSystem(url, umlsToken);

        if (vsacResponse.getStatus().equals("ok")) {
            cqlCode.setErrorMessage(null);
            cqlCode.setErrorCode(null);
            cqlCode.addValidatedWithVsac(VsacStatus.VALID);
        } else {
            cqlCode.addValidatedWithVsac(VsacStatus.IN_VALID);

            if (vsacResponse.getErrors() == null || CollectionUtils.isEmpty(vsacResponse.getErrors().getResultSet())) {
                cqlCode.setErrorMessage(vsacResponse.getMessage());
                cqlCode.setErrorCode(null);
            } else {

                List<String> strList = vsacResponse.getErrors().getResultSet()
                        .stream()
                        .map(VsacResponse.VsacErrorResultSet::getErrDesc)
                        .collect(Collectors.toList());

                cqlCode.setErrorMessage(String.join(", ", strList));
                cqlCode.setErrorCode(vsacResponse.getErrors().getResultSet().get(0).getErrCode());
            }
        }
    }

}
