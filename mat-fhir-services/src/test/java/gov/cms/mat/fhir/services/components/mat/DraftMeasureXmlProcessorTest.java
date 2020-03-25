package gov.cms.mat.fhir.services.components.mat;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.ResourceFileUtil;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.components.xml.MatXmlProcessor;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.CQLLibraryTranslationService;
import gov.cms.mat.fhir.services.service.orchestration.LibraryOrchestrationValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DraftMeasureXmlProcessorTest implements ResourceFileUtil {

    private static final String MEASURE_ID = "measureId";
    private static final String MEASURE_XML = "</xml>";

    private String cqlLookUpXml;
    private String convertedCql;

    @Mock
    private HapiFhirServer hapiFhirServer;
    @Mock
    private MatXmlProcessor matXmlProcessor;
    @Mock
    private MatXpath matXpath;
    @Mock
    private LibraryOrchestrationValidationService libraryOrchestrationValidationService;
    @Mock
    private CQLLibraryTranslationService cqlLibraryTranslationService;

    @InjectMocks
    private DraftMeasureXmlProcessor draftMeasureXmlProcessor;

    @BeforeEach
    public void setUp() {
        cqlLookUpXml = getStringFromResource("/cqlLookUpXmlFhir.xml");
        convertedCql = getStringFromResource("/translated.cql");
    }

    @Test
    void process() {

        ConversionReporter.setInThreadLocal(MEASURE_ID,
                "TEST",
                mock(ConversionResultsService.class),
                Instant.now(),
                ConversionType.CONVERSION,
                XmlSource.SIMPLE,
                Boolean.TRUE,
                null);

        Measure measure = new Measure();

        when(matXmlProcessor.getXml(measure, XmlSource.MEASURE)).thenReturn(MEASURE_XML.getBytes());
        when(matXpath.toQualityData(MEASURE_XML)).thenReturn(cqlLookUpXml);
        when(matXpath.processXmlValue(cqlLookUpXml, "library")).thenReturn("CWP_HEDIS_2020");
        when(matXpath.processXmlValue(cqlLookUpXml, "version")).thenReturn("1.2.000");
        when(hapiFhirServer.getBaseURL()).thenReturn("http://mick.mouse.com/");

        when(cqlLibraryTranslationService.convertCqlToJson(anyString(), any(), anyString(), any())).thenReturn("json");

        when(cqlLibraryTranslationService.convertMatXmlToCql(any(), anyString())).thenReturn(convertedCql);

        draftMeasureXmlProcessor.process(measure);
    }
}