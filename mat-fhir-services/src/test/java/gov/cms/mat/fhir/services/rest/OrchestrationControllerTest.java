package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.rest.dto.ConversionResultDto;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultProcessorService;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.service.MeasureDataService;
import gov.cms.mat.fhir.services.service.orchestration.OrchestrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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

    @InjectMocks
    private OrchestrationController orchestrationController;

    @Test
    void translateMeasureById() {
        Measure matMeasure = new Measure();
        when(measureDataService.findOneValid(MEASURE_ID)).thenReturn(matMeasure);

        ConversionResultDto conversionResultToReturn = new ConversionResultDto();
        when(conversionResultProcessorService.process(MEASURE_ID)).thenReturn(conversionResultToReturn);

        ConversionResultDto conversionResultReturned =
                orchestrationController.translateMeasureById(MEASURE_ID, ConversionType.VALIDATION, XmlSource.SIMPLE);

        assertEquals(conversionResultToReturn, conversionResultReturned);

        verify(measureDataService).findOneValid(MEASURE_ID);
        verify(orchestrationService).validate(any());
        verify(conversionResultProcessorService).process(MEASURE_ID);
    }
}