package gov.cms.mat.fhir.services.components.validation;

import gov.cms.mat.fhir.services.components.vsac.VsacResponse;
import gov.cms.mat.fhir.services.components.vsac.VsacRestClient;
import gov.cms.mat.fhir.services.service.VsacService;
import mat.model.cql.CQLCode;
import mat.model.cql.VsacStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vsac.VSACResponseResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

@ExtendWith(MockitoExtension.class)
class CodeSystemVsacAsyncTest {
    private static final String TOKEN = "token";
    private static final String TICKET = "a-golden-ticket-charlie";

    private static final String URL = "http://terminology.hl7.org/CodeSystem/request-intent";

    CQLCode cqlCode;
    @Mock
    private VsacService vsacService; //todo needs to be removed when value set refactored
    @InjectMocks
    private CodeSystemVsacAsync codeSystemVsacAsync;
    @Mock
    private VsacRestClient vsacRestClient;

    @BeforeEach
    void setUp() {
        cqlCode = new CQLCode();
        cqlCode.setCodeIdentifier("CODE:/CodeSystem/LOINC/Version/2.66/Code/21112-8/Info");
    }

    @Test
    void validateCodeTicketInvalid() throws ExecutionException, InterruptedException {

        VsacResponse vsacResponse = new VsacResponse();
        vsacResponse.setStatus("error");
        vsacResponse.setMessage("Can't log in");
        when(vsacRestClient.fetchCodeSystem("/CodeSystem/LOINC/Version/2.66/Code/21112-8/Info", TOKEN))
                .thenReturn(vsacResponse);


        CompletableFuture<Void> completableFuture = codeSystemVsacAsync.validateCode(cqlCode, TOKEN);
        completableFuture.get();

        assertEquals("Can't log in", cqlCode.getErrorMessage());
        assertEquals(VsacStatus.IN_VALID, cqlCode.obtainValidatedWithVsac());

        verifyNoMoreInteractions(vsacRestClient);
    }

    @Test
    void validateCodeInvalid() throws ExecutionException, InterruptedException {
        cqlCode.setCodeIdentifier("VALUESET:/CodeSystem/LOINC/Version/2.66/Code/21112-8/Info");

        CompletableFuture<Void> completableFuture = codeSystemVsacAsync.validateCode(cqlCode, TOKEN);
        completableFuture.get();

        assertEquals("Invalid code system uri", cqlCode.getErrorMessage());
        assertEquals(VsacStatus.IN_VALID, cqlCode.obtainValidatedWithVsac());

        verifyNoInteractions(vsacRestClient);
    }

    @Test
    void validateCodeBlankUrl() throws ExecutionException, InterruptedException {
        cqlCode.setCodeIdentifier("");

        CompletableFuture<Void> completableFuture = codeSystemVsacAsync.validateCode(cqlCode, TOKEN);
        completableFuture.get();

        assertEquals("URL is required", cqlCode.getErrorMessage());
        assertEquals(VsacStatus.IN_VALID, cqlCode.obtainValidatedWithVsac());

        verifyNoInteractions(vsacRestClient);
    }

    @Test
    void validateVsacServiceReturningErrors() throws ExecutionException, InterruptedException {
        VsacResponse vsacResponse = new VsacResponse();
        vsacResponse.setStatus("error");
        vsacResponse.setMessage("All Bad");
        vsacResponse.setErrors(new VsacResponse.VsacError());

        VsacResponse.VsacErrorResultSet error1 = new VsacResponse.VsacErrorResultSet();
        error1.setErrCode("1");
        error1.setErrDesc("Error 1");
        vsacResponse.getErrors().getResultSet().add(error1);

        VsacResponse.VsacErrorResultSet error2 = new VsacResponse.VsacErrorResultSet();
        error2.setErrCode("2");
        error2.setErrDesc("Error 2");
        vsacResponse.getErrors().getResultSet().add(error2);

        when(vsacRestClient.fetchCodeSystem("/CodeSystem/LOINC/Version/2.66/Code/21112-8/Info", TOKEN))
                .thenReturn(vsacResponse);

        CompletableFuture<Void> completableFuture = codeSystemVsacAsync.validateCode(cqlCode, TOKEN);
        completableFuture.get();

        assertEquals("Error 1, Error 2", cqlCode.getErrorMessage());
        assertEquals(VsacStatus.IN_VALID, cqlCode.obtainValidatedWithVsac());
    }
}