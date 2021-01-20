package gov.cms.mat.fhir.services.components.validation;

import gov.cms.mat.fhir.services.CqlHelper;
import gov.cms.mat.fhir.services.rest.dto.LibraryErrors;
import mat.model.cql.CQLCode;
import mat.model.cql.CQLCodeSystem;
import mat.model.cql.CQLModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeSystemValidatorTest implements CqlHelper {
    private static final String TOKEN = "token";
    private static final String  API_KEY = "api_key";

    @Mock
    private VsacCodeSystemValidator vsacCodeSystemValidator;
    @InjectMocks
    private CodeSystemValidator codeSystemValidator;

    private CQLModel cqlModel;
    private String cql;

    @BeforeEach
    void setUp() {
        cqlModel = loadMatXml("/test-cql/convert-1-mat.xml");
        cql = getStringFromResource("/test-cql/convert-1.cql");
    }

    @Test
    void validateNofFailingCodes() throws ExecutionException, InterruptedException {
        when(vsacCodeSystemValidator.validate(0, cqlModel.getCodeList(), TOKEN, API_KEY)).thenReturn(Collections.emptyList());

        CompletableFuture<List<LibraryErrors>> completableFuture =
                codeSystemValidator.validate(0, cqlModel.getCodeList(), cqlModel.getCodeSystemList(), cql, TOKEN, API_KEY);

        List<LibraryErrors> libraryErrors = completableFuture.get();

        assertTrue(libraryErrors.isEmpty());
    }

    @Test
    void validateFailingCodesGeneratingCodeErrors() throws ExecutionException, InterruptedException {
        List<CQLCode> failingCodes = List.of(cqlModel.getCodeList().get(0), cqlModel.getCodeList().get(1));

        cqlModel.getCodeList().get(0).setLineNumber(122);
        cqlModel.getCodeList().get(1).setLineNumber(144);

        // when error code is null or 802 (VSAC error when cannot find code) then we use the line number form the CODE
        cqlModel.getCodeList().get(0).setErrorCode(null);
        cqlModel.getCodeList().get(0).setErrorCode("802");

        when(vsacCodeSystemValidator.validate(0, cqlModel.getCodeList(), TOKEN, API_KEY)).thenReturn(failingCodes);

        CompletableFuture<List<LibraryErrors>> completableFuture =
                codeSystemValidator.validate(0, cqlModel.getCodeList(), cqlModel.getCodeSystemList(), cql, TOKEN, API_KEY);

        List<LibraryErrors> libraryErrors = completableFuture.get();

        assertEquals(1, libraryErrors.size());
        assertEquals(2, libraryErrors.get(0).getErrors().size());
        assertEquals(122, libraryErrors.get(0).getErrors().get(0).getErrorInLine());
        assertEquals(144, libraryErrors.get(0).getErrors().get(1).getErrorInLine());
    }

    @Test
    void validateFailingCodesGeneratingCodeSystemErrors() throws ExecutionException, InterruptedException {
        CQLCode cqlCode1 = cqlModel.getCodeList().get(0);
        CQLCode cqlCode2 = cqlModel.getCodeList().get(1);

        List<CQLCode> failingCodes = List.of(cqlCode1, cqlCode2);

        cqlModel.getCodeSystemList().stream()
                .filter(codeSystem -> codeSystem.getCodeSystemName().equals(cqlCode1.getCodeSystemName()))
                .forEach(cqlCodeSystem -> {
                    cqlCodeSystem.setLineNumber(222);
                });

        cqlModel.getCodeSystemList().stream()
                .filter(codeSystem -> codeSystem.getCodeSystemName().equals(cqlCode2.getCodeSystemName()))
                .forEach(cqlCodeSystem -> {
                    cqlCodeSystem.setLineNumber(244);
                });

        // when error code is null or 802 (VSAC error when cannot find code) then we use the line number form the CODE
        cqlCode1.setErrorCode("800");
        cqlCode2.setErrorCode("801");

        when(vsacCodeSystemValidator.validate(0, cqlModel.getCodeList(), TOKEN, API_KEY)).thenReturn(failingCodes);

        CompletableFuture<List<LibraryErrors>> completableFuture =
                codeSystemValidator.validate(0, cqlModel.getCodeList(), cqlModel.getCodeSystemList(), cql, TOKEN, API_KEY);

        List<LibraryErrors> libraryErrors = completableFuture.get();

        assertEquals(1, libraryErrors.size());
        assertEquals(2, libraryErrors.get(0).getErrors().size());
        assertEquals(222, libraryErrors.get(0).getErrors().get(0).getErrorInLine());
        assertEquals(244, libraryErrors.get(0).getErrors().get(1).getErrorInLine());
    }


    @Test
    void validateUnreferencedCodeSystems() throws ExecutionException, InterruptedException {
        when(vsacCodeSystemValidator.validate(0, cqlModel.getCodeList(), TOKEN,API_KEY))
                .thenReturn(Collections.emptyList());

        CQLCodeSystem cqlCodeSystem = new CQLCodeSystem();
        cqlCodeSystem.setCodeSystemName("Not-Found");
        cqlCodeSystem.setLineNumber(111);
        cqlModel.getCodeSystemList().add(cqlCodeSystem);

        CompletableFuture<List<LibraryErrors>> completableFuture =
                codeSystemValidator.validate(0, cqlModel.getCodeList(), cqlModel.getCodeSystemList(), cql, TOKEN, API_KEY);

        List<LibraryErrors> libraryErrors = completableFuture.get();

        assertEquals(1, libraryErrors.size());
        assertEquals(1, libraryErrors.get(0).getErrors().size());
        assertEquals(111, libraryErrors.get(0).getErrors().get(0).getErrorInLine());
        assertEquals("Code System is not referenced.", libraryErrors.get(0).getErrors().get(0).getErrorMessage());
        assertEquals("Error", libraryErrors.get(0).getErrors().get(0).getSeverity());
    }

    @Test
    void validateUnreferencedCode() throws ExecutionException, InterruptedException {
        CQLCode cqlCode = new CQLCode();
        cqlCode.setCodeSystemName("Not-Found");
        cqlCode.setLineNumber(666);
        cqlCode.setErrorCode("801");
        cqlCode.setName("I_HAVE_NO_SYSTEM");
        cqlCode.setErrorMessage("No Code System");

        when(vsacCodeSystemValidator.validate(0, cqlModel.getCodeList(), TOKEN, API_KEY))
                .thenReturn(List.of(cqlCode));

        CompletableFuture<List<LibraryErrors>> completableFuture =
                codeSystemValidator.validate(0, cqlModel.getCodeList(), cqlModel.getCodeSystemList(), cql, TOKEN, API_KEY);

        List<LibraryErrors> libraryErrors = completableFuture.get();

        assertEquals(1, libraryErrors.size());
        assertEquals(1, libraryErrors.get(0).getErrors().size());
        assertEquals(666, libraryErrors.get(0).getErrors().get(0).getErrorInLine());
        assertEquals("No Code System", libraryErrors.get(0).getErrors().get(0).getErrorMessage());
        assertEquals("Error", libraryErrors.get(0).getErrors().get(0).getSeverity());
    }
}