package gov.cms.mat.fhir.services.components.validation;


import gov.cms.mat.vsac.VsacService;
import gov.cms.mat.vsac.model.CodeSystemVersionResponse;
import gov.cms.mat.vsac.model.VsacCode;
import lombok.extern.slf4j.Slf4j;
import mat.model.cql.CQLCode;
import mat.model.cql.VsacStatus;
import mat.shared.CQLModelValidator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static gov.cms.mat.fhir.services.cql.parser.CqlUtils.parseCodeSystemName;
import static gov.cms.mat.fhir.services.cql.parser.CqlUtils.parseMatVersionFromCodeSystemUri;

@Component
@Slf4j
public class CodeSystemVsacAsync extends VsacValidator {
    public static final String REQUIRES_VALIDATION = "Code system requires validation. Please login to UMLS to validate it.";
    public static final String NOT_FOUND = "Code system not found in VSAC.";
    public static final String NOT_IN_VSAC = "NOT.IN.VSAC";
    public static final String CODE_IDENTIFIER_FORMAT = "CODE:/CodeSystem/%1$s/Version/%2$s/Code/%3$s/Info";
    private static final String INVALID_CODE_URL = "Invalid code system uri.";
    private static final String CODE_SYSTEM_NAME_IS_INVALID = "Code system name: %s not found in UMLS! Please verify the code system name.";
    private static final String VSAC_INTERNAL_ERROR = "Error calling UMLS. Please try again later.";

    CodeSystemVsacAsync(VsacService vsacService) {
        super(vsacService);
    }

    @Async("codeSystemTheadPoolValidation")
    CompletableFuture<Void> validateCode(CQLCode cqlCode, String umlsToken, String apiKey) {

        if (StringUtils.contains(cqlCode.getCodeSystemOID(), NOT_IN_VSAC)) {
            log.debug("No need to process NOT.IN.VSAC cqlCode: {}", cqlCode.getCodeSystemName());
        } else {
            try {
                isDirectReferenceCodeValid(cqlCode, umlsToken, apiKey);
                log.debug("Validated code {} with vsac {} message: {}",
                        cqlCode.getCodeIdentifier(),
                        cqlCode.isValidatedWithVsac(),
                        cqlCode.getErrorMessage());

            } catch (VsacCodeSystemValidatorException vc) {
                log.warn("VsacCodeSystemValidatorException: ", vc);
                cqlCode.setErrorMessage(vc.getMessage());
                cqlCode.addValidatedWithVsac(VsacStatus.IN_VALID);
            } catch (Exception e) {
                log.warn("validateCode exception :", e);
                cqlCode.addValidatedWithVsac(VsacStatus.IN_VALID);
                cqlCode.setErrorMessage(cqlCode.obtainValidatedWithVsac() == VsacStatus.PENDING ?
                        REQUIRES_VALIDATION : NOT_FOUND);
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    private String getCodeSystemUrlFromMatUrl(String codeSystemUrl) {
        String result = codeSystemUrl;
        int firstColon = codeSystemUrl.indexOf(":");
        if (firstColon >= 0) {
            result = codeSystemUrl.substring(firstColon + 1);
        }
        return result;
    }

    private void isDirectReferenceCodeValid(CQLCode cqlCode, String umlsToken, String apiKey) {
        String url = buildCodeIdentifier(cqlCode, umlsToken, apiKey);
        if (StringUtils.isNotBlank(url)) {
            CQLModelValidator validator = new CQLModelValidator();

            if (validator.validateForCodeIdentifier(url)) {
                throw new VsacCodeSystemValidatorException(INVALID_CODE_URL);
            }

            VsacCode vsacResponse = vsacService.getCode(getCodeSystemUrlFromMatUrl(url), umlsToken, apiKey);

            if (vsacResponse.getStatus().equals("ok")) {
                cqlCode.setErrorMessage(null);
                cqlCode.setErrorCode(null);
                cqlCode.setCodeIdentifier(url);
                cqlCode.addValidatedWithVsac(VsacStatus.VALID);
            } else {
                cqlCode.addValidatedWithVsac(VsacStatus.IN_VALID);

                if (vsacResponse.getErrors() == null || CollectionUtils.isEmpty(vsacResponse.getErrors().getResultSet())) {
                    cqlCode.setErrorMessage(vsacResponse.getMessage());
                    cqlCode.setErrorCode(null);
                } else {

                    List<String> strList = vsacResponse.getErrors().getResultSet()
                            .stream()
                            .map(VsacCode.VsacErrorResultSet::getErrDesc)
                            .collect(Collectors.toList());

                    cqlCode.setErrorMessage(String.join(", ", strList));
                    cqlCode.setErrorCode(vsacResponse.getErrors().getResultSet().get(0).getErrCode());
                }
            }
        }
    }


    /**
     * Builds the code identifier vsac url.
     *
     * @param cqlCode The CQLCode to build the Identifier for.
     * @return The VSAC code url, null if an issue occurred connecting to vsac. In the null case,
     * the error code, message, and validateWithVsac are populated.
     */
    private String buildCodeIdentifier(CQLCode cqlCode, String ulmsToken, String apiKey) {
        String result = null;
        boolean needVersion = StringUtils.isBlank(cqlCode.getCodeSystemVersionUri());
        if (needVersion) {
            String versionUri = parseMatVersionFromCodeSystemUri(cqlCode.getCodeSystemVersionUri());

            if (StringUtils.isBlank(versionUri)) {

                if (StringUtils.isBlank(cqlCode.getCodeSystemName())) {
                    throw new VsacCodeSystemValidatorException(String.format(CODE_SYSTEM_NAME_IS_INVALID, cqlCode.getCodeSystemName()));
                }

                //This hit is cached so no need to optimize.
                CodeSystemVersionResponse vsacResult =
                        vsacService.getCodeSystemVersionFromName(cqlCode.getCodeSystemName(), ulmsToken, apiKey);

                if (BooleanUtils.isTrue(vsacResult.getSuccess())) {
                    versionUri = vsacResult.getVersion();
                    result = String.format(CODE_IDENTIFIER_FORMAT,
                            parseCodeSystemName(cqlCode.getCodeSystemName()).getLeft(),
                            versionUri,
                            cqlCode.getCodeOID());
                } else if (StringUtils.equals(vsacResult.getMessage(), "CodeSystem not found.")) {
                    cqlCode.setErrorMessage(String.format(CODE_SYSTEM_NAME_IS_INVALID, cqlCode.getCodeSystemName()));
                    cqlCode.setErrorCode("802");
                    cqlCode.addValidatedWithVsac(VsacStatus.IN_VALID);
                } else {
                    cqlCode.setErrorMessage(StringUtils.isBlank(vsacResult.getMessage()) ? VSAC_INTERNAL_ERROR : vsacResult.getMessage());
                    cqlCode.setErrorCode("802");
                    cqlCode.addValidatedWithVsac(VsacStatus.PENDING);
                }
            }
        } else {
            result = String.format(CODE_IDENTIFIER_FORMAT,
                    parseCodeSystemName(cqlCode.getCodeSystemName()).getLeft(),
                    parseMatVersionFromCodeSystemUri(cqlCode.getCodeSystemVersionUri()),
                    cqlCode.getCodeOID());
        }
        return result;
    }


}
