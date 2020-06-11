package gov.cms.mat.fhir.services.components.validation;

import gov.cms.mat.fhir.services.service.VsacService;
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

    List<CQLQualityDataSetDTO> validate(List<CQLQualityDataSetDTO> valueSetList, String umlsToken) {
        if (StringUtils.isBlank(umlsToken)) {
            setAllNotValid(valueSetList, VsacStatus.IN_VALID, BLANK_UMLS_TOKEN);
        } else {
            setAllNotValid(valueSetList, VsacStatus.PENDING, null);

            List<CQLQualityDataSetDTO> validationList = valueSetList.stream()
                    .filter(c -> c.isValidatedWithVsac() != VsacStatus.VALID)
                    .collect(Collectors.toList());

            List<CompletableFuture<Void>> futures = new ArrayList<>();

            validationList.forEach(v -> {
                CompletableFuture<Void> completableFuture = valueSetVsacAsync.validateWithVsac(v, umlsToken);
                completableFuture.orTimeout(valueSetValidationPoolTimeout, TimeUnit.SECONDS);
                futures.add(completableFuture);
            });

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        }

        return valueSetList.stream()
                .filter(c -> c.isValidatedWithVsac() != VsacStatus.VALID)
                .collect(Collectors.toList());
    }

    private void setAllNotValid(List<CQLQualityDataSetDTO> valueSetList, VsacStatus status, String message) {
        valueSetList.stream()
                .filter(c -> c.isValidatedWithVsac() != VsacStatus.VALID)
                .forEach(c -> {
                    c.setValidatedWithVsac(status);
                    c.setErrorMessage(message);
                });
    }
}


