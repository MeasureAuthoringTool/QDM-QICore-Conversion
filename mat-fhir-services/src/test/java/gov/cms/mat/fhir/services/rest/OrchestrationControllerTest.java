package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.services.components.mongo.ConversionResultProcessorService;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.service.MeasureDataService;
import gov.cms.mat.fhir.services.service.orchestration.OrchestrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void translateMeasureById() {
        //todo fix when have the session key with time

//        Measure matMeasure = new Measure();
//        when(measureDataService.findOneValid(MEASURE_ID)).thenReturn(matMeasure);
//
//        ConversionResultDto conversionResultToReturn = new ConversionResultDto();
//        when(conversionResultProcessorService.process(MEASURE_ID)).thenReturn(conversionResultToReturn);
//
//        ConversionResultDto conversionResultReturned =
//                orchestrationController.translateMeasureById(MEASURE_ID, ConversionType.VALIDATION, XmlSource.SIMPLE);
//
//        assertEquals(conversionResultToReturn, conversionResultReturned);
//
//        verify(measureDataService).findOneValid(MEASURE_ID);
//        verify(orchestrationService).process(any());
//        verify(conversionResultProcessorService).process(MEASURE_ID);
    }
}