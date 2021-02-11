package gov.cms.mat.fhir.services.components.validation;

import gov.cms.mat.vsac.VsacService;
import lombok.extern.slf4j.Slf4j;
import mat.model.cql.CQLQualityDataSetDTO;
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
class VsacValueSetValidator extends VsacValidator {
    @Value("${valueset-validation-pool-timeout}")
    int valueSetValidationPoolTimeout;

    private final ValueSetVsacAsync valueSetVsacAsync;

    VsacValueSetValidator(VsacService vsacService, ValueSetVsacAsync valueSetVsacAsync) {
        super(vsacService);
        this.valueSetVsacAsync = valueSetVsacAsync;
    }

    List<CQLQualityDataSetDTO> validate(long timeout, List<CQLQualityDataSetDTO> valueSetList, String umlsToken, String apiKey) {
        long valueSetTimeout = Math.max(valueSetValidationPoolTimeout,timeout);
        if (StringUtils.isBlank(umlsToken)) {
            valueSetList.stream()
                    .filter(c -> c.obtainValidatedWithVsac() != VsacStatus.VALID)
                    .forEach(c -> c.setErrorMessage(c.obtainValidatedWithVsac() == VsacStatus.PENDING ?
                            ValueSetVsacAsync.REQURIES_VALIDATION:
                            ValueSetVsacAsync.NOT_FOUND));
        } else {
            List<CQLQualityDataSetDTO> validationList = valueSetList.stream()
                    .filter(c -> c.obtainValidatedWithVsac() != VsacStatus.VALID)
                    .collect(Collectors.toList());

            List<CompletableFuture<Void>> futures = new ArrayList<>();

            validationList.forEach(v -> {
                CompletableFuture<Void> completableFuture = valueSetVsacAsync.validateWithVsac(v, umlsToken, apiKey);
                completableFuture.orTimeout(valueSetTimeout, TimeUnit.SECONDS);
                futures.add(completableFuture);
            });

            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
            } catch (Exception e) {
                log.debug("Error waiting for work to complete.",e);
            }
        }

        return valueSetList.stream()
                .filter(c -> c.obtainValidatedWithVsac() != VsacStatus.VALID)
                .collect(Collectors.toList());
    }
}


