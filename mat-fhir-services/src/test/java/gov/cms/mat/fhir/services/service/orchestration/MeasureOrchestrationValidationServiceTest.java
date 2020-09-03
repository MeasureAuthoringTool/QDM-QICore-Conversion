package gov.cms.mat.fhir.services.service.orchestration;

import ca.uhn.fhir.context.FhirContext;
import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.reporting.ConversionReporter;
import gov.cms.mat.fhir.services.components.reporting.ConversionResultsService;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.config.HapiFhirConfig;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.summary.OrchestrationProperties;
import gov.cms.mat.fhir.services.translate.MeasureTranslator;
import org.hl7.fhir.r4.model.Enumerations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeasureOrchestrationValidationServiceTest {

    private final static String ID = "123";

    @Mock
    private HapiFhirServer hapiFhirServer;
    @Mock
    private MeasureTranslator measureTranslator;
    @Mock
    private ConversionResultsService conversionResultsService;


    @InjectMocks
    private MeasureOrchestrationValidationService measureOrchestrationValidationService;

    private Measure matMeasure;

    @BeforeEach
    void setUp() {
        ConversionReporter.setInThreadLocal(ID,
                "TEST",
                conversionResultsService,
                Instant.now(),
                ConversionType.CONVERSION,
                XmlSource.SIMPLE,
                Boolean.TRUE,
                null);

        matMeasure = new Measure();
        matMeasure.setId(ID);
    }

    @Test
    void validateNoPush() {
        OrchestrationProperties properties = OrchestrationProperties.builder()
                .matMeasure(matMeasure)
                .isPush(Boolean.FALSE)
                .build();

        boolean result = measureOrchestrationValidationService.validate(properties);
        assertTrue(result);

        verifyNoInteractions(hapiFhirServer, measureTranslator);
    }

    @Test
    void validatePushSuccess() {
        OrchestrationProperties properties = setUpPush();

        org.hl7.fhir.r4.model.Measure fhirMeasure = new org.hl7.fhir.r4.model.Measure();
        fhirMeasure.setStatus(Enumerations.PublicationStatus.DRAFT);

        when(measureTranslator.translateToFhir(ID)).thenReturn(fhirMeasure);

        boolean result = measureOrchestrationValidationService.validate(properties);
        assertTrue(result);
    }

    @Test
    void validatePushFailure() {
        OrchestrationProperties properties = setUpPush();

        org.hl7.fhir.r4.model.Measure fhirMeasure = new org.hl7.fhir.r4.model.Measure();

        when(measureTranslator.translateToFhir(ID)).thenReturn(fhirMeasure);

        boolean result = measureOrchestrationValidationService.validate(properties);
        assertFalse(result);
    }

    private OrchestrationProperties setUpPush() {
        HapiFhirConfig hapiFhirConfig = new HapiFhirConfig();
        //ReflectionTestUtils.setField(hapiFhirConfig,"profiles",new ArrayList<>());
        FhirContext ctx =  hapiFhirConfig.buildFhirContext();
        when(hapiFhirServer.getCtx()).thenReturn(ctx);

        return OrchestrationProperties.builder()
                .matMeasure(matMeasure)
                .isPush(Boolean.TRUE)
                .build();
    }
}