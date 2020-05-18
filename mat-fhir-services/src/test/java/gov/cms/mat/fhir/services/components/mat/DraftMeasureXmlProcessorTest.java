package gov.cms.mat.fhir.services.components.mat;

import gov.cms.mat.cql.dto.CqlConversionPayload;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

        setUpReporter();
    }

    @Test
    void process() {
        Measure measure = new Measure();

        when(matXmlProcessor.getXml(measure, XmlSource.MEASURE)).thenReturn(MEASURE_XML.getBytes());
        when(matXpath.toQualityData(MEASURE_XML)).thenReturn(cqlLookUpXml);
        when(matXpath.processXmlValue(cqlLookUpXml, "library")).thenReturn("CWP_HEDIS_2020");
        when(matXpath.processXmlValue(cqlLookUpXml, "version")).thenReturn("1.2.000");
        when(hapiFhirServer.getBaseURL()).thenReturn("http://mick.mouse.com/");

        CqlConversionPayload payload = CqlConversionPayload.builder()
                .json("json")
                .xml("xml")
                .build();

        when(cqlLibraryTranslationService.convertCqlToJson(anyString(), any(), anyString(), any(), anyBoolean()))
                .thenReturn(payload);

        when(cqlLibraryTranslationService.convertMatXmlToCql(any(), anyString(), anyBoolean())).thenReturn(convertedCql);

        when(cqlLibraryTranslationService.convertMatXmlToCql(any(), anyString(), anyBoolean())).thenReturn(convertedCql);

        draftMeasureXmlProcessor.processMeasure(measure, false);

        verify(matXmlProcessor).getXml(measure, XmlSource.MEASURE);
        verify(matXpath).toQualityData(MEASURE_XML);
        verify(matXpath).processXmlValue(cqlLookUpXml, "library");
        verify(matXpath).processXmlValue(cqlLookUpXml, "version");
    }

    private void setUpReporter() {
        ConversionReporter.setInThreadLocal(MEASURE_ID,
                "TEST",
                mock(ConversionResultsService.class),
                Instant.now(),
                ConversionType.CONVERSION,
                XmlSource.SIMPLE,
                Boolean.TRUE,
                null);
    }
}