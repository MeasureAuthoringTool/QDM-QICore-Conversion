package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.services.components.cql.FhirLibrarySourceProvider;
import gov.cms.mat.fhir.services.components.validation.CodeSystemValidator;
import gov.cms.mat.fhir.services.components.validation.ValueSetValidator;
import gov.cms.mat.fhir.services.rest.dto.CQLExpressionObject;
import gov.cms.mat.fhir.services.rest.dto.CQLObject;
import gov.cms.mat.fhir.services.rest.dto.LibraryErrors;
import gov.cms.mat.fhir.services.rest.dto.ValidationRequest;
import lombok.extern.slf4j.Slf4j;
import mat.CQLtoELM;
import mat.model.cql.CQLDefinition;
import mat.model.cql.CQLFunctions;
import mat.model.cql.CQLModel;
import mat.model.cql.CQLParameter;
import mat.server.CQLUtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.cql.model.DataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ValidationOrchestrationService {
    @Value("${validation-pool-timeout}")
    int validationPoolTimeOut;

    @Autowired
    FhirLibrarySourceProvider fhirLibrarySourceProvider;

    private CQLObject cqlObject = new CQLObject();

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
                                           List<LibraryErrors> errorsDetectedAlready,
                                           ValidationRequest validationRequest) {
        List<CompletableFuture<List<LibraryErrors>>> futures = new ArrayList<>();
        long validationTimeout = Math.max(validationRequest.getTimeoutSeconds(), validationPoolTimeOut);


        if (validationRequest.isValidateCqlToElm()) {
            CompletableFuture<List<LibraryErrors>> f = validationService.validateCql(cql);
            f.orTimeout(validationTimeout, TimeUnit.MINUTES);
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
                f.orTimeout(validationTimeout, TimeUnit.SECONDS);
                futures.add(f);
            }
        }

        if (validationRequest.isValidateCodeSystems()) {
            if (CollectionUtils.isEmpty(cqlModel.getCodeList()) && CollectionUtils.isEmpty(cqlModel.getCodeSystemList())) {
                log.debug("No code systems to validate for library: {}-{}", cqlModel.getLibraryName(), cqlModel.getVersionUsed());
            } else {
                CompletableFuture<List<LibraryErrors>> f =
                        codeSystemValidator.validate(validationRequest.getTimeoutSeconds(),
                                cqlModel.getCodeList(),
                                cqlModel.getCodeSystemList(),
                                cql,
                                ulmsToken);
                f.orTimeout(validationTimeout, TimeUnit.SECONDS);
                futures.add(f);
            }
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        } catch (Exception e) {
            log.debug("Error waiting for work to complete.",e);
        }

        List<LibraryErrors> libraryErrors =
                futures.stream()
                        .map(this::getFromFuture)
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(errorsDetectedAlready)) {
            libraryErrors.addAll(errorsDetectedAlready);
        }

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
        } catch (Exception e) {
            log.warn("Future DId not complete", e);
            return Collections.emptyList();
        }
    }

    public CQLObject buildCqlObject(CQLModel sourceModel) {
        String parentCQLString = CQLUtilityClass.getCqlString(sourceModel, "").getLeft();

        CQLtoELM cqlToELM = new CQLtoELM(parentCQLString, fhirLibrarySourceProvider);
        cqlToELM.doTranslation(true);

        for (CQLDefinition definition : sourceModel.getDefinitionList()) {
            String definitionName = definition.getName();
            CQLExpressionObject expression = new CQLExpressionObject("Definition", definitionName);
            DataType result = cqlToELM.getExpression(definitionName).getResultType();
            if (result != null) {
                expression.setReturnType(cqlToELM.getExpression(definitionName).getResultType().toString());
            }
            cqlObject.getCqlDefinitionObjectList().add(expression);
        }

        for (CQLFunctions function : sourceModel.getCqlFunctions()) {
            String functionName = function.getName();
            CQLExpressionObject expression = new CQLExpressionObject("Function", functionName);
            expression.setReturnType(cqlToELM.getExpressionReturnType(functionName));

            cqlObject.getCqlFunctionObjectList().add(expression);
        }

        for (CQLParameter parameter : sourceModel.getCqlParameters()) {
            String parameterName = parameter.getName();
            CQLExpressionObject expression = new CQLExpressionObject("Parameter", parameterName);
            cqlObject.getCqlParameterObjectList().add(expression);
        }

        return cqlObject;
    }
}
