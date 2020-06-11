package gov.cms.mat.fhir.services.components.validation;

import gov.cms.mat.fhir.services.service.VsacService;
import lombok.extern.slf4j.Slf4j;
import mat.model.cql.CQLQualityDataSetDTO;
import mat.model.cql.VsacStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.vsac.VSACResponseResult;

import java.util.concurrent.CompletableFuture;

import static gov.cms.mat.fhir.services.cql.parser.CqlUtils.parseOid;

@Component
@Slf4j
class ValueSetVsacAsync extends VsacValidator {
    @Value("${mat.qdm.default.expansion.id}")
    private String defaultExpId;

    ValueSetVsacAsync(VsacService vsacService) {
        super(vsacService);
    }

    @Async("valueSetTheadPoolValidation")
    CompletableFuture<Void> validateWithVsac(CQLQualityDataSetDTO code, String umlsToken) {
        try {
            String oid = parseOid(code.getOid());
            String fiveMinServiceTicket = fetchFiveMinuteTicket(umlsToken);

            boolean isValid = verifyWithVsac(oid, fiveMinServiceTicket);

            code.setErrorMessage(isValid ? null : NOT_IN_VSAC);
            code.setValidatedWithVsac(isValid ? VsacStatus.VALID : VsacStatus.IN_VALID);
        } catch (Exception e) {
            code.setValidatedWithVsac(VsacStatus.IN_VALID);
            code.setErrorMessage(e.getMessage());
        }

        return CompletableFuture.completedFuture(null);
    }

    private boolean verifyWithVsac(String oid, String fiveMinServiceTicket) {
        VSACResponseResult vsacResponseResult =
                vsacService.getMultipleValueSetsResponseByOID(oid.trim(), fiveMinServiceTicket, defaultExpId);

        if (vsacResponseResult != null && StringUtils.isNotBlank(vsacResponseResult.getXmlPayLoad())) {
            log.info("Successfully converted valueset object from vsac xml payload.");
            return true;
        } else {
            log.info("Unable to retrieve value set in VSAC.");
            return false;
        }
    }
}
