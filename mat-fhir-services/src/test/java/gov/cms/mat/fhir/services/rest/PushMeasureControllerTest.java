package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.rest.dto.PushValidationResult;
import gov.cms.mat.fhir.services.components.reporting.ConversionResultsService;
import gov.cms.mat.fhir.services.service.orchestration.PushMeasureService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PushMeasureControllerTest {
    private final static String ID = "id";

    @Mock
    private PushMeasureService pushMeasureService;

    @Mock
    private ConversionResultsService conversionResultsService;

    @InjectMocks
    private PushMeasureController pushMeasureController;

    @Test
    void convertStandAloneFromMatToFhir() {
        PushValidationResult pushValidationResult = new PushValidationResult();
        when(pushMeasureService.convert(anyString(), any())).thenReturn(pushValidationResult);

        PushValidationResult result = pushMeasureController.pushMeasure(ID, "batchId");

        assertEquals(pushValidationResult, result);
    }
}