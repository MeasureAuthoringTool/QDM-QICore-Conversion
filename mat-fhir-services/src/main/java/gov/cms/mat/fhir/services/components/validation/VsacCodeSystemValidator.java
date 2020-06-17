package gov.cms.mat.fhir.services.components.validation;

import gov.cms.mat.fhir.services.service.VsacService;
import lombok.extern.slf4j.Slf4j;
import mat.model.cql.CQLCode;
import mat.model.cql.VsacStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Component
@Slf4j
public class VsacCodeSystemValidator extends VsacValidator {
    @Value("${code-system-validation-pool-timeout}")
    int codeSystemValidationPoolTimeout;

    private final CodeSystemVsacAsync codeSystemVsacAsync;

    public VsacCodeSystemValidator(VsacService vsacService, CodeSystemVsacAsync codeSystemVsacAsync) {
        super(vsacService);
        this.codeSystemVsacAsync = codeSystemVsacAsync;
    }

    public List<CQLCode> validate(long timeout, List<CQLCode> codeList, String umlsToken) {
        long codeSystemTimeout = Math.max(timeout,codeSystemValidationPoolTimeout);
        if (StringUtils.isBlank(umlsToken)) {
            codeList.stream()
                    .filter(c -> c.obtainValidatedWithVsac() != VsacStatus.VALID)
                    .forEach(c -> {
                        c.setErrorMessage(c.obtainValidatedWithVsac() == VsacStatus.PENDING ?
                                CodeSystemVsacAsync.REQUIRES_VALIDATION:
                                CodeSystemVsacAsync.NOT_FOUND);
                    });
        } else {
            setAllNotValid(codeList, VsacStatus.PENDING, null);

            List<CQLCode> validationList = codeList.stream()
                    .filter(c -> c.obtainValidatedWithVsac() != VsacStatus.VALID)
                    .collect(Collectors.toList());

            List<CompletableFuture<Void>> futures = new ArrayList<>();

            validationList.forEach(cqlCode -> {
                CompletableFuture<Void> completableFuture = codeSystemVsacAsync.validateCode(cqlCode, umlsToken);
                completableFuture.orTimeout(codeSystemTimeout, TimeUnit.SECONDS);
                futures.add(completableFuture);
            });

            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
            } catch (Exception e) {
                log.debug("Error waiting for work to complete.",e);
            }
        }

        return codeList.stream()
                .filter(c -> c.obtainValidatedWithVsac() != VsacStatus.VALID)
                .collect(Collectors.toList());
    }

    private void setAllNotValid(List<CQLCode> codeList, VsacStatus status, String message) {
        codeList.stream()
                .filter(c -> c.obtainValidatedWithVsac() != VsacStatus.VALID)
                .forEach(c -> {
                    c.addValidatedWithVsac(status);
                    c.setErrorMessage(message);
                });
    }
}
