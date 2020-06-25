package gov.cms.mat.fhir.services.components.validation;

import gov.cms.mat.fhir.services.service.VsacService;
import mat.model.cql.CQLQualityDataSetDTO;
import mat.model.cql.VsacStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.vsac.VSACResponseResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@ExtendWith(MockitoExtension.class)
class ValueSetVsacAsyncTest {
    private static final String DEFAULT_EXP_ID = "default-exp-id";
    private static final String TOKEN = "token";
    private static final String TICKET = "a-golden-ticket-charlie";

    @Mock
    private VsacService vsacService;
    @InjectMocks
    private ValueSetVsacAsync valueSetVsacAsync;

    private CQLQualityDataSetDTO cqlQualityDataSetDTO;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(valueSetVsacAsync, "defaultExpId", DEFAULT_EXP_ID);
        cqlQualityDataSetDTO = new CQLQualityDataSetDTO();
        cqlQualityDataSetDTO.setOid("urn:oid:2.16.840.1.113883.17.4077.3.2056");
    }

    @Test
    void validateMissingTicket() throws ExecutionException, InterruptedException {
        when(vsacService.getServiceTicket(TOKEN)).thenReturn(null);
        cqlQualityDataSetDTO.addValidatedWithVsac(VsacStatus.PENDING);

        CompletableFuture<Void> completableFuture = valueSetVsacAsync.validateWithVsac(cqlQualityDataSetDTO, TOKEN);
        completableFuture.get();

        assertEquals("Value set requires validation. Please login to UMLS to validate it.", cqlQualityDataSetDTO.getErrorMessage());
        assertEquals(VsacStatus.IN_VALID, cqlQualityDataSetDTO.obtainValidatedWithVsac());

        verifyNoMoreInteractions(vsacService);
    }

    @Test
    void validateException() throws ExecutionException, InterruptedException {
        when(vsacService.getServiceTicket(TOKEN)).thenThrow(new RuntimeException("Golly Gee"));

        CompletableFuture<Void> completableFuture = valueSetVsacAsync.validateWithVsac(cqlQualityDataSetDTO, TOKEN);
        completableFuture.get();

        assertEquals("Value set not found in VSAC.", cqlQualityDataSetDTO.getErrorMessage());
          assertEquals(VsacStatus.IN_VALID, cqlQualityDataSetDTO.obtainValidatedWithVsac());

        verifyNoMoreInteractions(vsacService);
    }

    @Test
    void validateVsacServiceNotFinding() throws ExecutionException, InterruptedException {
        when(vsacService.getServiceTicket(TOKEN)).thenReturn(TICKET);

        VSACResponseResult vsacResponseResult = new VSACResponseResult();
        vsacResponseResult.setXmlPayLoad("");
        when(vsacService.getMultipleValueSetsResponseByOID("2.16.840.1.113883.17.4077.3.2056", TICKET, DEFAULT_EXP_ID))
                .thenReturn(vsacResponseResult);

        CompletableFuture<Void> completableFuture = valueSetVsacAsync.validateWithVsac(cqlQualityDataSetDTO, TOKEN);
        completableFuture.get();

        assertEquals("Value set not found in VSAC.", cqlQualityDataSetDTO.getErrorMessage());
        assertEquals(VsacStatus.IN_VALID, cqlQualityDataSetDTO.obtainValidatedWithVsac());
    }

    @Test
    void validateVsacServiceFound() throws ExecutionException, InterruptedException {

        when(vsacService.getServiceTicket(TOKEN)).thenReturn(TICKET);

        VSACResponseResult vsacResponseResult = new VSACResponseResult();
        vsacResponseResult.setXmlPayLoad("<xml>xml</xml>");
        when(vsacService.getMultipleValueSetsResponseByOID("2.16.840.1.113883.17.4077.3.2056", TICKET, DEFAULT_EXP_ID))
                .thenReturn(vsacResponseResult);

        CompletableFuture<Void> completableFuture = valueSetVsacAsync.validateWithVsac(cqlQualityDataSetDTO, TOKEN);
        completableFuture.get();

        assertNull(cqlQualityDataSetDTO.getErrorMessage());
        assertEquals(VsacStatus.VALID, cqlQualityDataSetDTO.obtainValidatedWithVsac());
    }
}