package gov.cms.mat.fhir.services.components.validation;

import gov.cms.mat.fhir.services.CqlHelper;
import gov.cms.mat.vsac.VsacService;
import mat.model.cql.CQLModel;
import mat.model.cql.CQLQualityDataSetDTO;
import mat.model.cql.VsacStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VsacValueSetValidatorTest implements CqlHelper {
    private static final String TOKEN = "token";
    private static final String API_KEY = "api-key";

    @Mock
    private VsacService vsacService;
    @Mock
    private ValueSetVsacAsync valueSetVsacAsync;

    @InjectMocks
    private VsacValueSetValidator vsacValueSetValidator;

    private CQLModel cqlModel;

    @BeforeEach
    void setUp() {
        cqlModel = loadMatXml("/test-cql/convert-fhir-mat.xml");
        ReflectionTestUtils.setField(vsacValueSetValidator, "valueSetValidationPoolTimeout", 30);
    }

    @Test
    void validateBlankTokenStatusPending() {
        cqlModel.getValueSetList().forEach(c -> c.addValidatedWithVsac(VsacStatus.PENDING));
        //cqlModel.getValueSetList().forEach(c -> c.obtainValidatedWithVsac());

        List<CQLQualityDataSetDTO> dtoList = vsacValueSetValidator.validate(0, cqlModel.getValueSetList(), "", API_KEY);

        assertEquals(cqlModel.getValueSetList().size(), dtoList.size());

        dtoList.forEach(d -> assertEquals("Value set requires validation. Please login to UMLS to validate it.", d.getErrorMessage()));
        dtoList.forEach(d -> assertEquals(VsacStatus.PENDING, d.obtainValidatedWithVsac()));
        verifyNoInteractions(vsacService);
    }

    @Test
    void validateBlankTokenStatusValid() {
        cqlModel.getValueSetList().forEach(c -> c.addValidatedWithVsac(VsacStatus.IN_VALID));

        List<CQLQualityDataSetDTO> dtoList = vsacValueSetValidator.validate(0, cqlModel.getValueSetList(), "", API_KEY);


        dtoList.forEach(d -> assertEquals("Value set not found in VSAC.", d.getErrorMessage()));
        dtoList.forEach(d -> assertEquals(VsacStatus.IN_VALID, d.obtainValidatedWithVsac()));
        verifyNoInteractions(vsacService);
    }

    @Test
    void validateAllValidatedWithVsac() {
        cqlModel.getValueSetList().forEach(c -> c.addValidatedWithVsac(VsacStatus.VALID));

        List<CQLQualityDataSetDTO> dtoList = vsacValueSetValidator.validate(0, cqlModel.getValueSetList(), TOKEN, API_KEY);

        assertTrue(dtoList.isEmpty()); // since all we valid nothing to do to create errors

        verifyNoInteractions(vsacService);
    }

    @Test
    void validateCompletableFutureProcessing() {
        cqlModel.getValueSetList().forEach(c -> c.addValidatedWithVsac(VsacStatus.IN_VALID));
        when(valueSetVsacAsync.validateWithVsac(any(), anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(null));

        List<CQLQualityDataSetDTO> dtoList = vsacValueSetValidator.validate(0, cqlModel.getValueSetList(), TOKEN, API_KEY);

        assertEquals(cqlModel.getValueSetList().size(), dtoList.size());

        verify(valueSetVsacAsync, times(cqlModel.getValueSetList().size())).validateWithVsac(any(), anyString(), anyString());
    }
}