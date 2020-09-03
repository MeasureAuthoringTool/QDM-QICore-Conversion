package gov.cms.mat.fhir.services.components.validation;

import gov.cms.mat.fhir.services.components.vsac.ValueSetVSACResponseResult;
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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


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

        assertEquals("VSAC ticket has expired. Please log into ULMS again.", cqlQualityDataSetDTO.getErrorMessage());
        assertEquals(VsacStatus.PENDING, cqlQualityDataSetDTO.obtainValidatedWithVsac());

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

        ValueSetVSACResponseResult vsacResponseResult = ValueSetVSACResponseResult.builder()
                .xmlPayLoad("")
                .isFailResponse(true)
                .build();

        when(vsacService.getValueSetVSACResponseResult("2.16.840.1.113883.17.4077.3.2056", TICKET))
                .thenReturn(vsacResponseResult);

        CompletableFuture<Void> completableFuture = valueSetVsacAsync.validateWithVsac(cqlQualityDataSetDTO, TOKEN);
        completableFuture.get();

        assertEquals("Value set not found in VSAC.", cqlQualityDataSetDTO.getErrorMessage());
        assertEquals(VsacStatus.IN_VALID, cqlQualityDataSetDTO.obtainValidatedWithVsac());
    }

    @Test
    void validateVsacServiceFound() throws ExecutionException, InterruptedException {

        when(vsacService.getServiceTicket(TOKEN)).thenReturn(TICKET);

        ValueSetVSACResponseResult vsacResponseResult = ValueSetVSACResponseResult.builder()
                .xmlPayLoad("<xml>xml</xml>")
                .isFailResponse(false)
                .build();

        when(vsacService.getValueSetVSACResponseResult("2.16.840.1.113883.17.4077.3.2056", TICKET))
                .thenReturn(vsacResponseResult);

        CompletableFuture<Void> completableFuture = valueSetVsacAsync.validateWithVsac(cqlQualityDataSetDTO, TOKEN);
        completableFuture.get();

        assertNull(cqlQualityDataSetDTO.getErrorMessage());
        assertEquals(VsacStatus.VALID, cqlQualityDataSetDTO.obtainValidatedWithVsac());
    }
}