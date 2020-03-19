package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.rest.dto.ConversionOutcome;
import gov.cms.mat.fhir.services.components.mongo.ConversionResult;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.exceptions.MeasureNotFoundException;
import gov.cms.mat.fhir.services.service.orchestration.VSACOrchestrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValueSetBackGroundServiceTest {
    private static final String ID = "id";

    @Mock
    private ConversionResultsService conversionResultsService;
    @Mock
    private MeasureDataService measureDataService;
    @Mock
    private VSACOrchestrationService vsacOrchestrationService;

    @InjectMocks
    private ValueSetBackGroundService valueSetBackGroundService;

    @Test
    void checkConversionResults_NoneReady() {
        when(conversionResultsService.findTopValueSetConversion()).thenReturn(Optional.empty());

        valueSetBackGroundService.checkConversionResults();

        verify(conversionResultsService).findTopValueSetConversion();
        verifyNoInteractions(measureDataService, vsacOrchestrationService);
    }

    @Test
    void checkConversionResults_OneReady_BadOutCome() {
        when(conversionResultsService.findTopValueSetConversion())
                .thenReturn(Optional.of(buildResult(ConversionOutcome.MEASURE_NOT_FOUND)))
                .thenReturn(Optional.empty());

        valueSetBackGroundService.checkConversionResults();

        verify(conversionResultsService, times(2)).findTopValueSetConversion();
        verifyNoInteractions(measureDataService, vsacOrchestrationService);
    }

    @Test
    void checkConversionResults_OneReady_MeasureInvalid() {
        when(conversionResultsService.findTopValueSetConversion())
                .thenReturn(Optional.of(buildResult(ConversionOutcome.SUCCESS_WITH_ERROR)))
                .thenReturn(Optional.empty());

        when(measureDataService.findOneValid(ID)).thenThrow(new MeasureNotFoundException(ID));

        valueSetBackGroundService.checkConversionResults();

        verify(conversionResultsService, times(2)).findTopValueSetConversion();
        verify(measureDataService).findOneValid(ID);
        verifyNoInteractions(vsacOrchestrationService);
    }

    @Test
    void checkConversionResults_OneReady_MeasureValid() {
        when(conversionResultsService.findTopValueSetConversion())
                .thenReturn(Optional.of(buildResult(ConversionOutcome.SUCCESS_WITH_ERROR)))
                .thenReturn(Optional.empty());

        when(measureDataService.findOneValid(ID)).thenReturn(new Measure());

        valueSetBackGroundService.checkConversionResults();

        verify(conversionResultsService, times(2)).findTopValueSetConversion();
        verify(measureDataService).findOneValid(ID);
        verify(vsacOrchestrationService).process(any());
    }

    private ConversionResult buildResult(ConversionOutcome conversionOutcome) {

        ConversionResult conversionResult = new ConversionResult();
        conversionResult.setShowWarnings(true);
        conversionResult.setMeasureId(ID);
        conversionResult.setOutcome(conversionOutcome);
        return conversionResult;
    }
}