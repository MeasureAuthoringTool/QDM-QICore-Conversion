package gov.cms.mat.fhir.services.components.validation;

import gov.cms.mat.fhir.services.CqlHelper;
import gov.cms.mat.fhir.services.service.VsacService;
import mat.model.cql.CQLCode;
import mat.model.cql.CQLModel;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ExtendWith(MockitoExtension.class)
class VsacCodeSystemValidatorTest implements CqlHelper {
    private static final String TOKEN = "token";

    @Mock
    private VsacService vsacService;
    @Mock
    private CodeSystemVsacAsync codeSystemVsacAsync;

    @InjectMocks
    private VsacCodeSystemValidator vsacCodeSystemValidator;

    private CQLModel cqlModel;

    @BeforeEach
    void setUp() {
        cqlModel = loadMatXml("/test-cql/convert-fhir-mat.xml");
        ReflectionTestUtils.setField(vsacCodeSystemValidator, "codeSystemValidationPoolTimeout", 30);
    }

    @Test
    void validateBlankToken() {
        cqlModel.getCodeList().forEach(c -> c.addValidatedWithVsac(VsacStatus.IN_VALID));
        List<CQLCode> dtoList = vsacCodeSystemValidator.validate(0, cqlModel.getCodeList(), "");

        assertEquals(cqlModel.getCodeList().size(), dtoList.size());

        dtoList.forEach(d -> assertEquals("Code system not found in VSAC.", d.getErrorMessage()));
        dtoList.forEach(d -> assertEquals(VsacStatus.IN_VALID, d.obtainValidatedWithVsac()));
        verifyNoInteractions(vsacService, codeSystemVsacAsync);
    }

    @Test
    void validateAllValidatedWithVsac() {
        cqlModel.getCodeList().forEach(c -> c.addValidatedWithVsac(VsacStatus.VALID));

        List<CQLCode> dtoList = vsacCodeSystemValidator.validate(0, cqlModel.getCodeList(), TOKEN);

        assertTrue(dtoList.isEmpty()); // since all we valid nothing to do to create errors

        verifyNoInteractions(vsacService, codeSystemVsacAsync);
    }

    @Test
    void validateCompletableFutureProcessing() {
        cqlModel.getCodeList().forEach(c -> c.addValidatedWithVsac(VsacStatus.IN_VALID));
        when(codeSystemVsacAsync.validateCode(any(), anyString())).thenReturn(CompletableFuture.completedFuture(null));

        List<CQLCode> dtoList = vsacCodeSystemValidator.validate(0, cqlModel.getCodeList(), TOKEN);

        assertEquals(cqlModel.getCodeList().size(), dtoList.size());

        verify(codeSystemVsacAsync, times(cqlModel.getCodeList().size())).validateCode(any(), anyString());
    }
}
