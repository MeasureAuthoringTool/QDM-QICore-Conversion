package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.commons.model.MeasureXml;
import gov.cms.mat.fhir.services.ResourceFileUtil;
import gov.cms.mat.fhir.services.cql.parser.AntlCqlParser;
import gov.cms.mat.fhir.services.cql.parser.CqlToMatXml;
import gov.cms.mat.fhir.services.cql.parser.CqlVisitorFactory;
import gov.cms.mat.fhir.services.cql.parser.ManageCodeListServiceImpl;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.repository.MeasureXmlRepository;
import gov.cms.mat.fhir.services.rest.dto.ValidationRequest;
import gov.cms.mat.fhir.services.service.MeasureDataService;
import gov.cms.mat.fhir.services.service.ValidationOrchestrationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatXmlControllerTest implements ResourceFileUtil {
    private static final String ID = "id";
    private static String ULMS_TOKEN = "token";
    private static String API_KEY = "api-key";

    @Mock
    private MeasureXmlRepository measureXmlRepository;
    @Mock
    private CqlLibraryRepository cqlLibraryRepository;
    @Mock
    private CqlVisitorFactory cqlVisitorFactory;
    @Mock
    private AntlCqlParser cqlParser;
    @Mock
    private ValidationOrchestrationService validationOrchestrationService;
    @Mock
    private MeasureDataService measureDataService;

    @InjectMocks
    private MatXmlController matXmlController;

    MockHttpServletResponse mockHttpServletResponse;

    @BeforeEach
    void setUp() {
        mockHttpServletResponse = new MockHttpServletResponse();
    }

    @Test
    void fromStandaloneLibCqlLibraryNotFound() {
        when(cqlLibraryRepository.findById(ID)).thenReturn(Optional.empty());
        MatXmlController.MatXmlReq matXmlReq = new MatXmlController.MatXmlReq();


        Assertions.assertThrows(ResponseStatusException.class, () -> {
            matXmlController.fromStandaloneLib(ULMS_TOKEN, API_KEY, ID, matXmlReq, mockHttpServletResponse);
        });

        verifyNoMoreInteractions(cqlLibraryRepository);

        verifyNoInteractions(measureXmlRepository,
                cqlVisitorFactory,
                cqlParser,
                validationOrchestrationService,
                measureDataService);
    }


    @Test
    void fromStandaloneLibCqlLibraryBlank() {
        CqlLibrary cqlLibrary = new CqlLibrary();
        when(cqlLibraryRepository.findById(ID)).thenReturn(Optional.of(cqlLibrary));
        MatXmlController.MatXmlReq matXmlReq = new MatXmlController.MatXmlReq();


        //  when(cqlParser.parse(anyString(), any())).thenCallRealMethod();


        Assertions.assertThrows(ResponseStatusException.class, () -> {
            matXmlController.fromStandaloneLib(ULMS_TOKEN, API_KEY, ID, matXmlReq, mockHttpServletResponse);
        });

        verifyNoMoreInteractions(cqlLibraryRepository);

        verifyNoInteractions(measureXmlRepository,
                cqlVisitorFactory,
                cqlParser,
                validationOrchestrationService,
                measureDataService);
    }


    @Test
    void fromStandaloneLib() {

//        when(validationOrchestrationService.validateCql(anyString(),
//                any(CQLModel.class),
//                anyString(),
//                any(), any(ValidationRequest.class))).thenReturn(Collections.emptyList());

        CqlLibrary cqlLibrary = new CqlLibrary();
        String cqlXml = getStringFromResource("/cqlLookUp.xml");
        cqlLibrary.setCqlXml(cqlXml);
        when(cqlLibraryRepository.findById(ID)).thenReturn(Optional.of(cqlLibrary));
        MatXmlController.MatXmlReq matXmlReq = new MatXmlController.MatXmlReq();

        ManageCodeListServiceImpl manageCodeListService = mock(ManageCodeListServiceImpl.class);
        when(cqlVisitorFactory.getCqlToMatXmlVisitor())
                .thenReturn(new CqlToMatXml(manageCodeListService, cqlLibraryRepository));

        doCallRealMethod().when(cqlParser).parse(anyString(), any(CqlToMatXml.class));


        MatXmlController.MatXmlResponse response =
                matXmlController.fromStandaloneLib(ULMS_TOKEN, API_KEY, ID, matXmlReq, mockHttpServletResponse);

        assertEquals("AIS_HEDIS_2020", response.getCqlModel().getLibraryName());
        assertEquals("1.0.000", response.getCqlModel().getVersionUsed());
        assertTrue(response.getCql().startsWith("library AIS_HEDIS_2020 version '1.0.000'"));
    }

    @Test
    void fromMeasureNotFound() {
        when(measureXmlRepository.findByMeasureId(ID)).thenReturn(Optional.empty());

        MatXmlController.MatXmlReq matXmlReq = new MatXmlController.MatXmlReq();

        ResponseStatusException exception = Assertions.assertThrows(ResponseStatusException.class, () -> {
            matXmlController.fromMeasure(ULMS_TOKEN, API_KEY, ID, matXmlReq, mockHttpServletResponse);
        });

        assertNotNull(exception.getReason());
        assertTrue(exception.getReason().startsWith("MEASURE not found "));

        verifyNoMoreInteractions(measureXmlRepository);

        verifyNoInteractions(cqlLibraryRepository,
                cqlVisitorFactory,
                cqlParser,
                validationOrchestrationService,
                measureDataService);
    }

    @Test
    void fromMeasureFoundNoXml() {
        MeasureXml measureXml = new MeasureXml();
        when(measureXmlRepository.findByMeasureId(ID)).thenReturn(Optional.of(measureXml));

        MatXmlController.MatXmlReq matXmlReq = new MatXmlController.MatXmlReq();

        ResponseStatusException exception = Assertions.assertThrows(ResponseStatusException.class, () -> {
            matXmlController.fromMeasure(ULMS_TOKEN, API_KEY, ID, matXmlReq, mockHttpServletResponse);
        });

        assertNotNull(exception.getReason());
        assertTrue(exception.getReason().startsWith("MEASURE_XML.XML does not exist "));

        verifyNoMoreInteractions(measureXmlRepository);

        verifyNoInteractions(cqlLibraryRepository,
                cqlVisitorFactory,
                cqlParser,
                validationOrchestrationService,
                measureDataService);
    }

    @Test
    void fromMeasure() {
        String matXml = getStringFromResource("/MeasureTranslator/mat.xml");
        MeasureXml measureXml = new MeasureXml();
        measureXml.setMeasureXml(matXml.getBytes(StandardCharsets.UTF_8));
        when(measureXmlRepository.findByMeasureId(ID)).thenReturn(Optional.of(measureXml));
        Measure measure = new Measure();

        when(measureDataService.findOneValid(ID)).thenReturn(measure);


        ManageCodeListServiceImpl manageCodeListService = mock(ManageCodeListServiceImpl.class);
        when(cqlVisitorFactory.getCqlToMatXmlVisitor())
                .thenReturn(new CqlToMatXml(manageCodeListService, cqlLibraryRepository));

        MatXmlController.MatXmlReq matXmlReq = new MatXmlController.MatXmlReq();
        matXmlReq.setValidationRequest(new ValidationRequest());


        MatXmlController.MatXmlResponse response =    matXmlController.fromMeasure(ULMS_TOKEN, API_KEY, ID, matXmlReq, mockHttpServletResponse);

        assertTrue(response.getCql().startsWith("library CDAYTest version '1.0.000'"));
    }

    @Test
    void fromCql() {
        MatXmlController.MatCqlXmlReq matCqlXmlReq = new MatXmlController.MatCqlXmlReq();
        matCqlXmlReq.setValidationRequest(new ValidationRequest());

        ManageCodeListServiceImpl manageCodeListService = mock(ManageCodeListServiceImpl.class);
        when(cqlVisitorFactory.getCqlToMatXmlVisitor())
                .thenReturn(new CqlToMatXml(manageCodeListService, cqlLibraryRepository));


        MatXmlController.MatXmlResponse response =    matXmlController.fromCql(ULMS_TOKEN, API_KEY,  matCqlXmlReq, mockHttpServletResponse);

        assertNull(response.getCql());
    }
}