package gov.cms.mat.fhir.services.components.validation;

import gov.cms.mat.fhir.services.CqlHelper;
import gov.cms.mat.fhir.services.rest.dto.LibraryErrors;
import mat.model.cql.CQLCode;
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
        when(vsacCodeSystemValidator.validate(0, cqlModel.getCodeList(), TOKEN)).thenReturn(Collections.emptyList());

        CompletableFuture<List<LibraryErrors>> completableFuture =
                codeSystemValidator.validate(0, cqlModel.getCodeList(), cqlModel.getCodeSystemList(), cql, TOKEN);

        List<LibraryErrors> libraryErrors = completableFuture.get();

        assertTrue(libraryErrors.isEmpty());
    }

    @Test
    void validateFailingCodes() throws ExecutionException, InterruptedException {
        List<CQLCode> failingCodes = List.of(cqlModel.getCodeList().get(0), cqlModel.getCodeList().get(1));

        when(vsacCodeSystemValidator.validate(0, cqlModel.getCodeList(), TOKEN)).thenReturn(failingCodes);

        CompletableFuture<List<LibraryErrors>> completableFuture =
                codeSystemValidator.validate(0, cqlModel.getCodeList(), cqlModel.getCodeSystemList(), cql, TOKEN);

        List<LibraryErrors> libraryErrors = completableFuture.get();

        assertEquals(1, libraryErrors.size());
        assertEquals(2, libraryErrors.get(0).getErrors().size());
        assertEquals(10, libraryErrors.get(0).getErrors().get(0).getErrorInLine());
        assertEquals(11, libraryErrors.get(0).getErrors().get(1).getErrorInLine());
    }
}