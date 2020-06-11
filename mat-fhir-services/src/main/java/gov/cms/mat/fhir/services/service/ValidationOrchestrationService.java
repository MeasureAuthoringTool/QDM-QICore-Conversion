package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.services.components.validation.CodeSystemValidator;
import gov.cms.mat.fhir.services.components.validation.ValueSetValidator;
import gov.cms.mat.fhir.services.rest.dto.LibraryErrors;
import gov.cms.mat.fhir.services.rest.dto.ValidationRequest;
import lombok.extern.slf4j.Slf4j;
import mat.model.cql.CQLModel;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ValidationOrchestrationService {
    @Value("${validation-pool-timeout}")
    int validationPoolTimeOut;

    private final ValidationService validationService;
    private final ValueSetValidator valueSetValidator;
    private final CodeSystemValidator codeSystemValidator;

    public ValidationOrchestrationService(ValidationService validationService,
                                          ValueSetValidator valueSetValidator, CodeSystemValidator codeSystemValidator) {
        this.validationService = validationService;
        this.valueSetValidator = valueSetValidator;
        this.codeSystemValidator = codeSystemValidator;
    }

    public List<LibraryErrors> validateCql(String cql,
                                           CQLModel cqlModel,
                                           String ulmsToken,
                                           ValidationRequest validationRequest) {
        List<CompletableFuture<List<LibraryErrors>>> futures = new ArrayList<>();
        long validationTimeout = Math.max(validationRequest.getTimeoutSeconds(),validationPoolTimeOut);


        if (validationRequest.isValidateCqlToElm()) {
            CompletableFuture<List<LibraryErrors>> f = validationService.validateCql(cql);
            f.orTimeout(validationTimeout, TimeUnit.SECONDS); //todo add this as config yaml parameter
            futures.add(f);
        }

        if (validationRequest.isValidateValueSets()) {
            if (CollectionUtils.isEmpty(cqlModel.getValueSetList())) {
                log.debug("No value sets to validate for library: {}-{}",
                        cqlModel.getLibraryName(), cqlModel.getVersionUsed());
            } else {
                CompletableFuture<List<LibraryErrors>> f =
                        valueSetValidator.validate(validationRequest.getTimeoutSeconds(),
                                cqlModel.getValueSetList(),
                                cql,
                                ulmsToken);
                f.orTimeout(validationTimeout, TimeUnit.SECONDS); //todo add this as config yaml parameter
                futures.add(f);
            }
        }

        if (validationRequest.isValidateCodeSystems()) {
            if (CollectionUtils.isEmpty(cqlModel.getCodeList())) {
                log.debug("No code systems to validate for library: {}-{}", cqlModel.getLibraryName(), cqlModel.getVersionUsed());
            } else {
                CompletableFuture<List<LibraryErrors>> f =
                       codeSystemValidator.validate(validationRequest.getTimeoutSeconds(),
                               cqlModel.getCodeList(),
                               cql,
                               ulmsToken);
                f.orTimeout(validationTimeout, TimeUnit.SECONDS); //todo add this as config yaml parameter
                futures.add(f);
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        List<LibraryErrors> libraryErrors =
                futures.stream()
                        .map(this::getFromFuture)
                        .flatMap(List::stream)
                        .collect(Collectors.toList());

        if (libraryErrors.isEmpty()) {
            return libraryErrors;
        } else {
            return mergeAndSort(libraryErrors);
        }
    }

    private List<LibraryErrors> mergeAndSort(List<LibraryErrors> libraryErrors) {
        List<LibraryErrors> combined = new ArrayList<>();

        libraryErrors.forEach(libError -> {
            if (combined.contains(libError)) {
                LibraryErrors existing = findError(combined, libError);
                existing.getErrors().addAll(libError.getErrors());
            } else {
                combined.add(libError);
            }
        });

        combined.forEach(c -> c.getErrors().sort(null)); // will use default compareTo

        return combined;

    }

    private LibraryErrors findError(List<LibraryErrors> combined, LibraryErrors libraryError) {
        return combined.stream()
                .filter(c -> c.equals(libraryError))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot find Error"));  // should not happen
    }

    private List<LibraryErrors> getFromFuture(CompletableFuture<List<LibraryErrors>> l) {
        try {
            return l.get();
        } catch (InterruptedException | ExecutionException e) {
            log.warn("Future DId not complete", e);
            return Collections.emptyList();
        }
    }
}