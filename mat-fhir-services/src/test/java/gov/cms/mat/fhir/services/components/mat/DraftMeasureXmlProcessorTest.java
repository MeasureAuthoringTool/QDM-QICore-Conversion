package gov.cms.mat.fhir.services.components.mat;

import gov.cms.mat.cql.dto.CqlConversionPayload;
import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.ResourceFileUtil;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.components.xml.MatXmlProcessor;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.config.HapiFhirConfig;
import gov.cms.mat.fhir.services.exceptions.HapiResourceValidationException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.service.CQLLibraryTranslationService;
import gov.cms.mat.fhir.services.service.orchestration.LibraryOrchestrationValidationService;
import gov.cms.mat.fhir.services.translate.LibraryTranslator;
import gov.cms.mat.fhir.services.translate.creators.FhirCreator;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Library;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import static gov.cms.mat.fhir.services.translate.LibraryTranslator.SYSTEM_CODE;
import static gov.cms.mat.fhir.services.translate.LibraryTranslator.SYSTEM_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class DraftMeasureXmlProcessorTest implements ResourceFileUtil, FhirCreator {

    private static final String MEASURE_ID = "measureId";
    private static final String MEASURE_XML = "<xml>measure</xml>";
    private static final String LIBRARY_ID = "123";
    private static final String XML = "</xml>";
    private static final String JSON = "{}";

    private String cqlLookUpXml;
    private String convertedCql;

    private CqlConversionPayload payload;

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
    @Mock
    private LibraryTranslator libraryTranslator;
    @Mock
    private CqlLibraryRepository cqlLibRepo;

    @InjectMocks
    private DraftMeasureXmlProcessor draftMeasureXmlProcessor;

    @BeforeEach
    public void setUp() {
        cqlLookUpXml = getStringFromResource("/cqlLookUpXmlFhir.xml");
        convertedCql = getStringFromResource("/translated.cql");

        setUpReporter();

        payload = CqlConversionPayload.builder()
                .json(JSON)
                .xml(XML)
                .build();
    }


    @Test
    public void pushStandAloneFhirValidatorFails() {
        String matXml = setUpStandAlone();

        Library library = new Library();

        when(libraryTranslator.translateToFhir(LIBRARY_ID, convertedCql, XML, JSON)).thenReturn(library);


        Exception exception = assertThrows(HapiResourceValidationException.class, () -> {
            draftMeasureXmlProcessor.pushStandAlone(LIBRARY_ID, matXml);
        });

        //Could not validate hapi Library with id: 123
        assertTrue(exception.getMessage().contains(LIBRARY_ID));
        verify(hapiFhirServer, times(0)).persist(any());
    }

    @Test
    public void pushStandAloneFhirValidatorSuccess() {
        String matXml = setUpStandAlone();

        Library library = new Library();
        library.setStatus(Enumerations.PublicationStatus.DRAFT);
        library.setType(createType(SYSTEM_TYPE, SYSTEM_CODE));
        // this all I need to make library pass

        when(libraryTranslator.translateToFhir(LIBRARY_ID, convertedCql, XML, JSON)).thenReturn(library);

        when(hapiFhirServer.persist(library)).thenReturn(LIBRARY_ID);

        String result = draftMeasureXmlProcessor.pushStandAlone(LIBRARY_ID, matXml);

        assertEquals(LIBRARY_ID, result);
    }

    public String setUpStandAlone() {
        HapiFhirConfig hapiFhirConfig = new HapiFhirConfig();

        when(hapiFhirServer.getCtx()).thenReturn(hapiFhirConfig.buildFhirContext());

        String matXml = getStringFromResource("/MeasureTranslator/fhir-simple.xml");
        when(matXpath.processXmlValue(matXml, "library")).thenCallRealMethod();
        when(matXpath.processXmlValue(matXml, "version")).thenCallRealMethod();


        when(cqlLibraryTranslationService.convertMatXmlToCql(matXml,
                "SepsisAntibioticsOrdered-2.1.004",
                false)).thenReturn(convertedCql);

        when(cqlLibraryTranslationService.convertCqlToJson(eq("SepsisAntibioticsOrdered-2.1.004"),
                any(AtomicBoolean.class),
                eq(convertedCql),
                eq(CQLLibraryTranslationService.ConversionType.FHIR),
                anyBoolean())).thenReturn(payload);

        return matXml;
    }

    @Test
    void process() {
        Measure measure = new Measure();
        measure.setId("1234567890");

        when(matXmlProcessor.getXml(measure, XmlSource.MEASURE)).thenReturn(MEASURE_XML.getBytes());
        when(matXpath.toQualityData(MEASURE_XML)).thenReturn(cqlLookUpXml);
        when(matXpath.processXmlValue(cqlLookUpXml, "library")).thenReturn("CWP_HEDIS_2020");
        when(matXpath.processXmlValue(cqlLookUpXml, "version")).thenReturn("1.2.000");

        when(cqlLibraryTranslationService.convertCqlToJson(anyString(), any(), anyString(), any(), anyBoolean()))
                .thenReturn(payload);

        when(cqlLibraryTranslationService.convertMatXmlToCql(any(), anyString(), anyBoolean())).thenReturn(convertedCql);

        when(cqlLibraryTranslationService.convertMatXmlToCql(any(), anyString(), anyBoolean())).thenReturn(convertedCql);

        CqlLibrary lib = new CqlLibrary();
        lib.setId(LIBRARY_ID);
        lib.setMeasureId(measure.getId());
        when(cqlLibRepo.getCqlLibraryByMeasureId(eq(measure.getId()))).thenReturn(lib);

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