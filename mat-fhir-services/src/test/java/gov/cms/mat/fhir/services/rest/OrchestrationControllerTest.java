package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.rest.dto.ConversionResultDto;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.reporting.ConversionResultProcessorService;
import gov.cms.mat.fhir.services.components.reporting.ConversionResultsService;
import gov.cms.mat.fhir.services.components.reporting.ThreadSessionKey;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.MeasureNotFoundException;
import gov.cms.mat.fhir.services.exceptions.MeasureReleaseVersionInvalidException;
import gov.cms.mat.fhir.services.service.MeasureDataService;
import gov.cms.mat.fhir.services.service.orchestration.OrchestrationService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrchestrationControllerTest {
    private static final String MEASURE_ID = "measure_id";

    @Mock
    private OrchestrationService orchestrationService;
    @Mock
    private ConversionResultProcessorService conversionResultProcessorService;
    @Mock
    private MeasureDataService measureDataService;
    @Mock
    private ConversionResultsService conversionResultsService;

    @InjectMocks
    private OrchestrationController orchestrationController;

    @Test
    void translateMeasureByIdThrowMeasureReleaseVersionInvalidException() {
        when(measureDataService.findOneValid(MEASURE_ID))
                .thenThrow(new MeasureReleaseVersionInvalidException(MEASURE_ID, "1.0", "0.9"));

        ConversionResultDto conversionResultToReturn = new ConversionResultDto();
        when(conversionResultProcessorService.process(any(ThreadSessionKey.class))).thenReturn(conversionResultToReturn);

        ConversionResultDto conversionResultReturned =
            orchestrationController.translateMeasureById(MEASURE_ID,
                    ConversionType.VALIDATION,
                    XmlSource.SIMPLE,
                    "batchId",
                    true,
                    "vsacApiKey");

        assertEquals(conversionResultToReturn, conversionResultReturned);


        verifyNoMoreInteractions(measureDataService, conversionResultProcessorService);
        verifyNoInteractions(orchestrationService);

    }

    @Test
    void translateMeasureByIdThrowMeasureNotFoundException() {
        when(measureDataService.findOneValid(MEASURE_ID)).thenThrow(new MeasureNotFoundException(MEASURE_ID));

        ConversionResultDto conversionResultToReturn = new ConversionResultDto();
        when(conversionResultProcessorService.process(any(ThreadSessionKey.class))).thenReturn(conversionResultToReturn);

        ConversionResultDto conversionResultReturned =
            orchestrationController.translateMeasureById(MEASURE_ID,
                    ConversionType.VALIDATION,
                    XmlSource.MEASURE,
                    "batchId",
                    true,
                    "vsacApiKey");

        assertEquals(conversionResultToReturn, conversionResultReturned);

        verifyNoMoreInteractions(measureDataService, conversionResultProcessorService);
        verifyNoInteractions(orchestrationService);
    }

    @Test
    void translateMeasureById() {
        Measure matMeasure = new Measure();
        when(measureDataService.findOneValid(MEASURE_ID)).thenReturn(matMeasure);

        ConversionResultDto conversionResultToReturn = new ConversionResultDto();

        when(conversionResultProcessorService.process(any(ThreadSessionKey.class))).thenReturn(conversionResultToReturn);

        ConversionResultDto conversionResultReturned =
            orchestrationController.translateMeasureById(MEASURE_ID,
                    ConversionType.VALIDATION,
                    XmlSource.SIMPLE,
                    "batchId",
                    true,
                    "vsacApiKey");

        assertEquals(conversionResultToReturn, conversionResultReturned);

        verify(orchestrationService).process(any(OrchestrationProperties.class));

        verifyNoMoreInteractions(orchestrationService, measureDataService, conversionResultProcessorService);
    }
}