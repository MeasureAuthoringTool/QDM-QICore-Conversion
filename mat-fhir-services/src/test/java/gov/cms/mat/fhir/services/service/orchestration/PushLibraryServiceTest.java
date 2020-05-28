package gov.cms.mat.fhir.services.service.orchestration;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.rest.dto.ConversionResultDto;
import gov.cms.mat.fhir.services.components.mat.DraftMeasureXmlProcessor;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultProcessorService;
import gov.cms.mat.fhir.services.components.mongo.ThreadSessionKey;
import gov.cms.mat.fhir.services.exceptions.CqlConversionException;
import gov.cms.mat.fhir.services.service.CqlLibraryDataService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PushLibraryServiceTest {
    private static final String ID = "id";

    @Mock
    private LibraryOrchestrationService libraryOrchestrationService;
    @Mock
    private CqlLibraryDataService cqlLibraryDataService;
    @Mock
    private ConversionResultProcessorService conversionResultProcessorService;
    @Mock
    private DraftMeasureXmlProcessor draftMeasureXmlProcessor;

    @InjectMocks
    PushLibraryService pushLibraryService;

    @Test
    void convertQdmToFhirBadLibraryModel() {
        OrchestrationProperties orchestrationProperties = OrchestrationProperties.builder().build();
        CqlLibrary cqlLibrary = new CqlLibrary();
        when(cqlLibraryDataService.findCqlLibraryRequired(ID)).thenReturn(cqlLibrary);

        Exception exception = assertThrows(CqlConversionException.class, () -> {
            pushLibraryService.convertQdmToFhir(ID, orchestrationProperties);
        });

        assertEquals("Library is not QDM", exception.getMessage());

        verify(cqlLibraryDataService).findCqlLibraryRequired(ID);

        verifyNoInteractions(libraryOrchestrationService, conversionResultProcessorService, draftMeasureXmlProcessor);
    }

    @Test
    void convertQdmToFhirNotStandAlone() {
        ThreadSessionKey threadSessionKey = ThreadSessionKey.builder().measureId(ID).start(Instant.now()).build();

        OrchestrationProperties orchestrationProperties = OrchestrationProperties.builder()
                .threadSessionKey(threadSessionKey)
                .build();

        CqlLibrary cqlLibrary = new CqlLibrary();
        cqlLibrary.setLibraryModel("QDM");
        when(cqlLibraryDataService.findCqlLibraryRequired(ID)).thenReturn(cqlLibrary);

        ConversionResultDto conversionResultDto = new ConversionResultDto();
        when(conversionResultProcessorService.processLibrary(threadSessionKey)).thenReturn(conversionResultDto);

        ConversionResultDto result = pushLibraryService.convertQdmToFhir(ID, orchestrationProperties);

        assertEquals(result, conversionResultDto);

        verify(cqlLibraryDataService).findCqlLibraryRequired(ID);
        verify(libraryOrchestrationService).process(orchestrationProperties);
        verify(conversionResultProcessorService).processLibrary(threadSessionKey);

        verifyNoInteractions(draftMeasureXmlProcessor);
    }

    @Test
    void convertStandAloneFromMatToFhir() {
        String xml = "</xml>";
        OrchestrationProperties orchestrationProperties = OrchestrationProperties.builder()
                .build();

        CqlLibrary cqlLibrary = new CqlLibrary();
        cqlLibrary.setCqlXml(xml);
        cqlLibrary.setLibraryModel("FHIR");
        when(cqlLibraryDataService.findCqlLibraryRequired(ID)).thenReturn(cqlLibrary);

        when(draftMeasureXmlProcessor.pushStandAlone(ID, xml)).thenReturn("YOUR_A_OKAY");

        String result = pushLibraryService.convertStandAloneFromMatToFhir(ID, orchestrationProperties);
        assertEquals("YOUR_A_OKAY", result);

        verify(cqlLibraryDataService).findCqlLibraryRequired(ID);
        verify(draftMeasureXmlProcessor).pushStandAlone(ID, xml);
        verifyNoInteractions(libraryOrchestrationService, conversionResultProcessorService);
    }
}