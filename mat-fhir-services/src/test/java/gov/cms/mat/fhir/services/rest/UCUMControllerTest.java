package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.services.service.UCUMValidationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UCUMControllerTest {
    @Mock
    UCUMValidationService ucumValidationService;

    @InjectMocks
    private UCUMController ucumController;

    @Test
    void validateCode() {
        when(ucumValidationService.validate("K")).thenReturn(Boolean.TRUE);
        assertEquals(Boolean.TRUE, ucumController.validateCode("K"));
        verify(ucumValidationService).validate("K");
    }
}