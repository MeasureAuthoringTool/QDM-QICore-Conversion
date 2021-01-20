package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.rest.dto.ConversionOutcome;
import gov.cms.mat.fhir.rest.dto.ConversionResultDto;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.rest.dto.LibraryConversionResults;
import gov.cms.mat.fhir.rest.dto.MeasureConversionResults;
import gov.cms.mat.fhir.rest.dto.ValueSetConversionResults;
import gov.cms.mat.fhir.services.components.reporting.ConversionReporter;
import gov.cms.mat.fhir.services.components.reporting.ConversionResult;
import gov.cms.mat.fhir.services.components.reporting.ConversionResultProcessorService;
import gov.cms.mat.fhir.services.components.reporting.ConversionResultsService;
import gov.cms.mat.fhir.services.components.reporting.ThreadSessionKey;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.MeasureNotFoundException;
import gov.cms.mat.fhir.services.exceptions.MeasureReleaseVersionInvalidException;
import gov.cms.mat.fhir.services.service.MeasureDataService;
import gov.cms.mat.fhir.services.service.orchestration.OrchestrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VSACOrchestrationControllerTest {
    private static final String ID = "id";

    @Mock
    private OrchestrationService orchestrationService;
    @Mock
    private ConversionResultProcessorService conversionResultProcessorService;
    @Mock
    private MeasureDataService measureDataService;
    @Mock
    private ConversionResultsService conversionResultsService;

    @InjectMocks
    private VSACOrchestrationController vsacOrchestrationController;

    private Measure matMeasure;

    @BeforeEach
    void setUp() {
        matMeasure = new Measure();
        matMeasure.setId(ID);
    }

    @Test
    void translateMeasureByIdMeasureReleaseVersionInvalidException() {
        when(measureDataService.findOneValid(ID)).thenThrow(new MeasureReleaseVersionInvalidException(ID, "2.0", "3.0"));

        ConversionResultDto conversionResultDto = new ConversionResultDto();
        when(conversionResultProcessorService.process(any(ThreadSessionKey.class))).thenReturn(conversionResultDto);

        ConversionResultDto resultDto = translate();

        assertEquals(conversionResultDto, resultDto);
    }

    @Test
    void translateMeasureByIdMeasureNotFoundException() {
        when(measureDataService.findOneValid(ID)).thenThrow(new MeasureNotFoundException(ID));

        ConversionResultDto conversionResultDto = new ConversionResultDto();
        when(conversionResultProcessorService.process(any(ThreadSessionKey.class))).thenReturn(conversionResultDto);

        ConversionResultDto resultDto = translate();

        assertEquals(conversionResultDto, resultDto);
    }

    @Test
    void translateMeasureByIdWithConversionOutcome() {
        when(measureDataService.findOneValid(ID)).thenReturn(matMeasure);

        ConversionResult conversionResult = new ConversionResult();
        conversionResult.setOutcome(ConversionOutcome.SUCCESS);
        when(conversionResultsService.findConversionResult(any(ThreadSessionKey.class))).thenReturn(conversionResult);

        ConversionResultDto conversionResultDto = new ConversionResultDto();
        when(conversionResultProcessorService.process(any(ThreadSessionKey.class))).thenReturn(conversionResultDto);

        ConversionResultDto resultDto = translate();

        assertEquals(conversionResultDto, resultDto);
    }



    @Test
    void translateMeasureValueSetConversionFailure() {
        when(measureDataService.findOneValid(ID)).thenReturn(matMeasure);

        ConversionResult conversionResult = new ConversionResult();
        ValueSetConversionResults valueSetConversionResults = new ValueSetConversionResults();
        valueSetConversionResults.setSuccess(false);
        conversionResult.setValueSetConversionResults(List.of(valueSetConversionResults));

        when(conversionResultsService.findConversionResult(any(ThreadSessionKey.class))).thenReturn(conversionResult);

        ConversionResultDto conversionResultDto = new ConversionResultDto();
        when(conversionResultProcessorService.process(any(ThreadSessionKey.class))).thenReturn(conversionResultDto);

        ConversionResultDto resultDto = translate();

        assertEquals(conversionResultDto, resultDto);
    }

    @Test
    void translateMeasureLibraryFailure() {
        when(measureDataService.findOneValid(ID)).thenReturn(matMeasure);

        ConversionResult conversionResult = new ConversionResult();
        LibraryConversionResults libraryConversionResults = new LibraryConversionResults();
        libraryConversionResults.setSuccess(false);
        conversionResult.setLibraryConversionResults(List.of(libraryConversionResults));

        when(conversionResultsService.findConversionResult(any(ThreadSessionKey.class))).thenReturn(conversionResult);

        ConversionResultDto conversionResultDto = new ConversionResultDto();
        when(conversionResultProcessorService.process(any(ThreadSessionKey.class))).thenReturn(conversionResultDto);

        ConversionResultDto resultDto = translate();

        assertEquals(conversionResultDto, resultDto);
    }

    @Test
    void translateMeasureLibrarySuccess() {
        Instant.now(Clock.fixed(
                Instant.parse("2018-08-22T10:00:00Z"),
                ZoneOffset.UTC));


        when(measureDataService.findOneValid(ID)).thenReturn(matMeasure);

        ConversionResult conversionResult = new ConversionResult();
        LibraryConversionResults libraryConversionResults = new LibraryConversionResults();
        libraryConversionResults.setSuccess(true);
        conversionResult.setLibraryConversionResults(List.of(libraryConversionResults));

        ValueSetConversionResults valueSetConversionResults = new ValueSetConversionResults();
        valueSetConversionResults.setSuccess(true);
        conversionResult.setValueSetConversionResults(List.of(valueSetConversionResults));

        MeasureConversionResults measureConversionResults = new MeasureConversionResults();
        measureConversionResults.setSuccess(true);
        conversionResult.setMeasureConversionResults(measureConversionResults);


        when(conversionResultsService.findConversionResult(any(ThreadSessionKey.class))).thenReturn(conversionResult);

        ConversionResultDto conversionResultDto = new ConversionResultDto();
        when(conversionResultProcessorService.process(any(ThreadSessionKey.class))).thenReturn(conversionResultDto);

        ConversionResultDto resultDto = translate();

        assertEquals(conversionResultDto, resultDto);


        verify( conversionResultsService).addErrorMessage(any(), any(), any());
    }



    private ConversionResultDto translate() {
        return vsacOrchestrationController.translateMeasureById(ID,
                ConversionType.CONVERSION,
                XmlSource.MEASURE,
                "batchId",
                false,
                "vsacGrantingTicket");
    }
}