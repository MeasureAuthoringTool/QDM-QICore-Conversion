package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.rest.dto.ConversionResultDto;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.reporting.ConversionResultsService;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.service.orchestration.PushLibraryService;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StandAloneLibraryControllerTest {
    private static final String ID = "id";
    @Mock
    private ConversionResultsService conversionResultsService;
    @Mock
    private PushLibraryService pushLibraryService;
    @Mock
    private CqlLibraryRepository cqlLibraryRepository;

    @InjectMocks
    private StandAloneLibraryController standAloneLibraryController;

    @Test
    void convertQdmToFhir() {
        ConversionResultDto conversionResultDto = new ConversionResultDto();
        when(pushLibraryService.convertQdmToFhir(anyString(), any(OrchestrationProperties.class)))
                .thenReturn(conversionResultDto);

        ConversionResultDto result = standAloneLibraryController.convertQdmToFhir(ID,
                ConversionType.CONVERSION,
                true,
                "batchId");

        assertEquals(conversionResultDto, result);
    }

    @Test
    void pushStandAloneFromMatToFhir() {
        when(pushLibraryService.convertStandAloneFromMatToFhir(anyString(), any(OrchestrationProperties.class)))
                .thenReturn("RESULT");

        String result = standAloneLibraryController.pushStandAloneFromMatToFhir(ID, "batchId");
        assertEquals("RESULT", result);
    }

    @Test
    void pushAllVersionedLibs() {
        CqlLibrary cqlLibrary = new CqlLibrary();
        cqlLibrary.setId(ID);
        cqlLibrary.setCqlName("CqlName");
        cqlLibrary.setLibraryModel("FHIR");
        cqlLibrary.setVersion(new BigDecimal("1.0"));
        cqlLibrary.setRevisionNumber(2);

        when(cqlLibraryRepository.getAllVersionedCqlFhirLibs()).thenReturn(Collections.singletonList(ID));
        when(cqlLibraryRepository.getCqlLibraryById(Mockito.eq(ID))).thenReturn(cqlLibrary);

        StandAloneLibraryController.PushAllResult result = standAloneLibraryController.pushAllVersionedLibs();
        assertEquals(1, result.getSuccesses().size());
        assertEquals("id CqlName FHIR v1.0.002", result.getSuccesses().get(0));
    }
}