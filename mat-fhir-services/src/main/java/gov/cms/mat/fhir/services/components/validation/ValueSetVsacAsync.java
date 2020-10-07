package gov.cms.mat.fhir.services.components.validation;

import gov.cms.mat.vsac.VsacService;
import gov.cms.mat.vsac.model.ValueSetResult;
import lombok.extern.slf4j.Slf4j;
import mat.model.cql.CQLQualityDataSetDTO;
import mat.model.cql.VsacStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static gov.cms.mat.fhir.services.cql.parser.CqlUtils.parseOid;

@Component
@Slf4j
class ValueSetVsacAsync extends VsacValidator {
    public static final String REQURIES_VALIDATION = "Value set requires validation. Please login to UMLS to validate it.";
    public static final String NOT_FOUND = "Value set not found in VSAC.";
    public static final String TICKET_EXPIRED = "VSAC ticket has expired. Please log into ULMS again.";

    @Value("${mat.qdm.default.expansion.id}")
    private String defaultExpId;

    ValueSetVsacAsync(VsacService vsacService) {
        super(vsacService);
    }

    @Async("valueSetTheadPoolValidation")
    CompletableFuture<Void> validateWithVsac(CQLQualityDataSetDTO code, String umlsToken) {
        try {
            String oid = parseOid(code.getOid());

            boolean isValid = verifyWithVsac(oid, umlsToken);

            code.setErrorMessage(isValid ? null : NOT_FOUND);
            code.addValidatedWithVsac(isValid ? VsacStatus.VALID : VsacStatus.IN_VALID);
        } catch (ExpiredTicketException ete) {
            log.warn("Error validating ValueSetVsac with vsac oid: {}", code.getOid(), ete);
            code.setErrorMessage(TICKET_EXPIRED);
            code.addValidatedWithVsac(VsacStatus.PENDING);
        } catch (Exception e) {
            log.warn("Error validating ValueSetVsac with vsac oid: {}", code.getOid(), e);
            code.setErrorMessage(code.obtainValidatedWithVsac() == VsacStatus.PENDING ?
                    REQURIES_VALIDATION : NOT_FOUND);
            code.addValidatedWithVsac(VsacStatus.IN_VALID);
        }
        return CompletableFuture.completedFuture(null);
    }

    private boolean verifyWithVsac(String oid, String umlsToken) {
        ValueSetResult vsacResponseResult =
                vsacService.getValueSetResult(oid.trim(), umlsToken);

        if (vsacResponseResult != null && StringUtils.isNotBlank(vsacResponseResult.getXmlPayLoad())) {
            log.info("Successfully converted valueset object from vsac xml payload.");
            return true;
        } else {
            log.info("Unable to retrieve value set in VSAC.");
            return false;
        }
    }
}
