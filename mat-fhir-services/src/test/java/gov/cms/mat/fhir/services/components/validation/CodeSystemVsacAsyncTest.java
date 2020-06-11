package gov.cms.mat.fhir.services.components.validation;

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
    private VsacService vsacService;
    @InjectMocks
    private CodeSystemVsacAsync codeSystemVsacAsync;

    @BeforeEach
    void setUp() {
        cqlCode = new CQLCode();
        cqlCode.setCodeIdentifier("http://terminology.hl7.org/CodeSystem/request-intent");
    }

    @Test
    void validateCodeTicketInvalid() throws ExecutionException, InterruptedException {
        when(vsacService.getServiceTicket(TOKEN)).thenReturn(null);

        CompletableFuture<Void> completableFuture = codeSystemVsacAsync.validateCode(cqlCode, TOKEN);
        completableFuture.get();

        assertEquals("VSAC ticket has expired", cqlCode.getErrorMessage());
        assertEquals(VsacStatus.IN_VALID, cqlCode.obtainValidatedWithVsac());

        verifyNoMoreInteractions(vsacService);
    }

    @Test
    void validateCodeBlankUrl() throws ExecutionException, InterruptedException {
        cqlCode.setCodeIdentifier("");


        CompletableFuture<Void> completableFuture = codeSystemVsacAsync.validateCode(cqlCode, TOKEN);
        completableFuture.get();

        assertEquals("URL is required", cqlCode.getErrorMessage());
        assertEquals(VsacStatus.IN_VALID, cqlCode.obtainValidatedWithVsac());

        verifyNoInteractions(vsacService);
    }

    @Test
    void validateVsacServiceNotFinding() throws ExecutionException, InterruptedException {
        when(vsacService.getServiceTicket(TOKEN)).thenReturn(TICKET);

        VSACResponseResult vsacResponseResult = new VSACResponseResult();
        vsacResponseResult.setXmlPayLoad("");
        when(vsacService.getDirectReferenceCode(URL, TICKET))
                .thenReturn(vsacResponseResult);

        CompletableFuture<Void> completableFuture = codeSystemVsacAsync.validateCode(cqlCode, TOKEN);
        completableFuture.get();

        assertEquals("Not In Vsac", cqlCode.getErrorMessage());
        assertEquals(VsacStatus.IN_VALID, cqlCode.obtainValidatedWithVsac());
    }

    @Test
    void validateVsacServiceFound() throws ExecutionException, InterruptedException {

        when(vsacService.getServiceTicket(TOKEN)).thenReturn(TICKET);

        VSACResponseResult vsacResponseResult = new VSACResponseResult();
        vsacResponseResult.setXmlPayLoad("<xml>xml</xml>");
        when(vsacService.getDirectReferenceCode(URL, TICKET))
                .thenReturn(vsacResponseResult);

        CompletableFuture<Void> completableFuture = codeSystemVsacAsync.validateCode(cqlCode, TOKEN);
        completableFuture.get();

        assertNull(cqlCode.getErrorMessage());
        assertEquals(VsacStatus.VALID, cqlCode.obtainValidatedWithVsac());
    }
}