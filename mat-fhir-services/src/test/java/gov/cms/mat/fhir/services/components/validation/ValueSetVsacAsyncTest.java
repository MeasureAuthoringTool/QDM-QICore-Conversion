package gov.cms.mat.fhir.services.components.validation;

import gov.cms.mat.vsac.VsacService;
import gov.cms.mat.vsac.model.ValueSetResult;
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
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ValueSetVsacAsyncTest {
    private static final String DEFAULT_EXP_ID = "default-exp-id";
    private static final String TOKEN = "token";


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
    void validateVsacServiceNotFinding() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> completableFuture = valueSetVsacAsync.validateWithVsac(cqlQualityDataSetDTO, TOKEN);
        completableFuture.get();

        assertEquals("Value set not found in VSAC.", cqlQualityDataSetDTO.getErrorMessage());
        assertEquals(VsacStatus.IN_VALID, cqlQualityDataSetDTO.obtainValidatedWithVsac());
    }

    @Test
    void validateVsacServiceFound() throws ExecutionException, InterruptedException {

        //when(vsacService.getServiceTicket(TOKEN)).thenReturn(TICKET);

        ValueSetResult vsacResponseResult = ValueSetResult.builder()
                .xmlPayLoad("<xml>xml</xml>")
                .isFailResponse(false)
                .build();

        when(vsacService.getValueSetResult("2.16.840.1.113883.17.4077.3.2056", TOKEN))
                .thenReturn(vsacResponseResult);

        CompletableFuture<Void> completableFuture = valueSetVsacAsync.validateWithVsac(cqlQualityDataSetDTO, TOKEN);
        completableFuture.get();

        assertNull(cqlQualityDataSetDTO.getErrorMessage());
        assertEquals(VsacStatus.VALID, cqlQualityDataSetDTO.obtainValidatedWithVsac());
    }
}