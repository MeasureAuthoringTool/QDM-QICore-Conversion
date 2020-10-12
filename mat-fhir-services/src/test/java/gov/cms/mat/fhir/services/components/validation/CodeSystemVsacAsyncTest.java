package gov.cms.mat.fhir.services.components.validation;


import gov.cms.mat.vsac.VsacService;
import gov.cms.mat.vsac.model.CodeSystemVersionResponse;
import mat.model.cql.CQLCode;
import mat.model.cql.VsacStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeSystemVsacAsyncTest {
    private static final String TOKEN = "token";

    CQLCode cqlCode;

    @InjectMocks
    private CodeSystemVsacAsync codeSystemVsacAsync;
    @Mock
    private VsacService vsacService;

    @BeforeEach
    void setUp() {
        cqlCode = new CQLCode();
        cqlCode.setCodeIdentifier("CODE:/CodeSystem/LOINC/Version/2.66/Code/21112-8/Info");
    }

    @Test
    void validateCodeTicketInvalid() throws ExecutionException, InterruptedException {

        CodeSystemVersionResponse vsacResponse = CodeSystemVersionResponse.builder()
                .success(false)
                .message("Can't log in")
                .build();

        cqlCode.setCodeSystemName("LOINC");

        when(vsacService.getCodeSystemVersionFromName("LOINC", TOKEN)).thenReturn(vsacResponse);

        CompletableFuture<Void> completableFuture = codeSystemVsacAsync.validateCode(cqlCode, TOKEN);
        completableFuture.get();

        assertEquals("Can't log in", cqlCode.getErrorMessage());
        assertEquals(VsacStatus.PENDING, cqlCode.obtainValidatedWithVsac());

        verifyNoMoreInteractions(vsacService);

    }


    @Test
    void validateCodeNotFound() throws ExecutionException, InterruptedException {

        CodeSystemVersionResponse vsacResponse = CodeSystemVersionResponse.builder()
                .success(false)
                .message("CodeSystem not found.")
                .build();

        cqlCode.setCodeSystemName("LOINC");

        when(vsacService.getCodeSystemVersionFromName("LOINC", TOKEN)).thenReturn(vsacResponse);

        CompletableFuture<Void> completableFuture = codeSystemVsacAsync.validateCode(cqlCode, TOKEN);
        completableFuture.get();

        assertEquals("Code system name: LOINC not found in UMLS! Please verify the code system name.", cqlCode.getErrorMessage());
        assertEquals(VsacStatus.IN_VALID, cqlCode.obtainValidatedWithVsac());
    }
}