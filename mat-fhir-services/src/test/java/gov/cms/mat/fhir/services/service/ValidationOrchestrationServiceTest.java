package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.services.CqlHelper;
import gov.cms.mat.fhir.services.components.validation.CodeSystemValidator;
import gov.cms.mat.fhir.services.components.validation.ValueSetValidator;
import gov.cms.mat.fhir.services.rest.dto.LibraryErrors;
import gov.cms.mat.fhir.services.rest.dto.ValidationRequest;
import mat.model.cql.CQLModel;
import mat.shared.CQLError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidationOrchestrationServiceTest implements CqlHelper {
    private static final String TOKEN = "token";
    private static final String LIB_NAME = "Covid-22";
    private static final String LIB_VERSION = "1.0.000";

    private static final int VALIDATION_POOL_TIME_OUT = 30;

    @Mock
    private ValidationService validationService;
    @Mock
    private ValueSetValidator valueSetValidator;
    @Mock
    private CodeSystemValidator codeSystemValidator;

    @InjectMocks
    private ValidationOrchestrationService validationOrchestrationService;

    private CQLModel cqlModel;
    private String cql;
    private LibraryErrors libraryErrors;
    private CQLError cqlError;

    @BeforeEach
    void setUp() {
        cqlModel = loadMatXml("/test-cql/convert-1-mat.xml");
        cql = getStringFromResource("/test-cql/convert-1.cql");

        libraryErrors = new LibraryErrors(LIB_NAME, LIB_VERSION);
        cqlError = buildError(Integer.MAX_VALUE);
        libraryErrors.getErrors().add(cqlError);

        ReflectionTestUtils.setField(validationOrchestrationService, "validationPoolTimeOut", VALIDATION_POOL_TIME_OUT);
    }

    @Test
    void validateCqlAllValidationsSetToFalse() {
        ValidationRequest validationRequest = getValidationRequest(false, false, false);

        List<LibraryErrors> libraryErrors =
                validationOrchestrationService.validateCql(cql, cqlModel, TOKEN, new ArrayList<>(), validationRequest);

        assertTrue(libraryErrors.isEmpty());

        verifyNoInteractions(validationService, valueSetValidator, codeSystemValidator);
    }

    @Test
    void validateCqlAllValidationsNoValueSetsAndCodeSystems() {
        cqlModel.setCodeList(Collections.emptyList());
        cqlModel.setValueSetList(Collections.emptyList());

        ValidationRequest validationRequest = getValidationRequest(true, true, false);

        List<LibraryErrors> libraryErrors =
                validationOrchestrationService.validateCql(cql, cqlModel, TOKEN, new ArrayList<>(), validationRequest);

        assertTrue(libraryErrors.isEmpty());

        verifyNoInteractions(validationService, valueSetValidator, codeSystemValidator);
    }

    @Test
    void validateCqlToElmFailure() {
        when(validationService.validateCql(cql)).thenReturn(CompletableFuture.failedFuture(new IOException("ohh darn")));

        ValidationRequest validationRequest = getValidationRequest(false, false, true);

        List<LibraryErrors> libraryErrors =
                validationOrchestrationService.validateCql(cql, cqlModel, TOKEN, new ArrayList<>(), validationRequest);

        assertTrue(libraryErrors.isEmpty());

        verifyNoInteractions(valueSetValidator, codeSystemValidator);
    }

    @Test
    void validateCqlToElm() {
        when(validationService.validateCql(cql)).thenReturn(CompletableFuture.completedFuture(List.of(libraryErrors)));

        ValidationRequest validationRequest = getValidationRequest(false, false, true);

        List<LibraryErrors> result =
                validationOrchestrationService.validateCql(cql, cqlModel, TOKEN, new ArrayList<>(), validationRequest);

        checkResult(result);

        verifyNoInteractions(valueSetValidator, codeSystemValidator);
    }


    @Test
    void validateValueSets() {
        when(valueSetValidator.validate(VALIDATION_POOL_TIME_OUT, cqlModel.getValueSetList(), cql, TOKEN))
                .thenReturn(CompletableFuture.completedFuture(List.of(libraryErrors)));

        ValidationRequest validationRequest = getValidationRequest(true, false, false);

        List<LibraryErrors> result =
                validationOrchestrationService.validateCql(cql, cqlModel, TOKEN, new ArrayList<>(), validationRequest);

        checkResult(result);

        verifyNoInteractions(validationService, codeSystemValidator);
    }

    @Test
    void validateCodeSystems() {
        when(codeSystemValidator.validate(VALIDATION_POOL_TIME_OUT, cqlModel.getCodeList(), cql, TOKEN))
                .thenReturn(CompletableFuture.completedFuture(List.of(libraryErrors)));

        ValidationRequest validationRequest = getValidationRequest(false, true, false);

        List<LibraryErrors> result =
                validationOrchestrationService.validateCql(cql, cqlModel, TOKEN, new ArrayList<>(), validationRequest);

        checkResult(result);

        verifyNoInteractions(validationService, valueSetValidator);
    }

    @Test
    void validateMergeAndSort() {

        CQLError middle = buildError(1000);
        libraryErrors.getErrors().add(middle);

        LibraryErrors externalLibError = new LibraryErrors("MY_LIB", "MY_VERSION"); // no errors
        CQLError externalError = buildError(1);
        externalLibError.getErrors().add(externalError);
        when(validationService.validateCql(cql)).thenReturn(CompletableFuture.completedFuture(List.of(libraryErrors, externalLibError)));

        LibraryErrors valueSetErrors = new LibraryErrors(LIB_NAME, LIB_VERSION); // no errors
        when(valueSetValidator.validate(VALIDATION_POOL_TIME_OUT, cqlModel.getValueSetList(), cql, TOKEN))
                .thenReturn(CompletableFuture.completedFuture(List.of(valueSetErrors)));

        LibraryErrors codeSystemErrors = new LibraryErrors(LIB_NAME, LIB_VERSION);
        CQLError start = buildError(1);
        codeSystemErrors.getErrors().add(start);

        when(codeSystemValidator.validate(VALIDATION_POOL_TIME_OUT, cqlModel.getCodeList(), cql, TOKEN))
                .thenReturn(CompletableFuture.completedFuture(List.of(codeSystemErrors)));


        ValidationRequest validationRequest = getValidationRequest(true, true, true);

        List<LibraryErrors> result =
                validationOrchestrationService.validateCql(cql, cqlModel, TOKEN, new ArrayList<>(), validationRequest);

        assertEquals(2, result.size());

        LibraryErrors err = result.get(0);
        assertEquals(LIB_NAME, err.getName());
        assertEquals(LIB_VERSION, err.getVersion());
        assertEquals(3, err.getErrors().size());

        assertEquals(start, err.getErrors().get(0));
        assertEquals(middle, err.getErrors().get(1));
        assertEquals(cqlError, err.getErrors().get(2));

        LibraryErrors external = result.get(1);

        assertEquals("MY_LIB", external.getName());
        assertEquals("MY_VERSION", external.getVersion());
        assertEquals(1, external.getErrors().size());
    }

    private void checkResult(List<LibraryErrors> result) {
        assertEquals(1, result.size());

        LibraryErrors err = result.get(0);
        assertEquals(1, err.getErrors().size());
        assertEquals(libraryErrors, err);
        assertEquals(cqlError, err.getErrors().get(0)); // Errors are not in equals
    }

    private ValidationRequest getValidationRequest(boolean validateValueSets,
                                                   boolean validateCodeSystems,
                                                   boolean validateCqlToElm) {
        ValidationRequest validationRequest = new ValidationRequest();
        validationRequest.setValidateValueSets(validateValueSets);
        validationRequest.setValidateCodeSystems(validateCodeSystems);
        validationRequest.setValidateCqlToElm(validateCqlToElm);

        validationRequest.setValidateSyntax(false); // todo add test for this when added

        validationRequest.setTimeoutSeconds(VALIDATION_POOL_TIME_OUT);
        return validationRequest;
    }

    private CQLError buildError(int errorInLine) {
        CQLError cqlError = new CQLError();
        cqlError.setErrorMessage("BAD ERROR " + errorInLine);
        cqlError.setSeverity("ERROR");
        cqlError.setErrorInLine(errorInLine);
        return cqlError;
    }
}