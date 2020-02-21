package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.components.xml.MatXmlProcessor;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import gov.cms.mat.fhir.services.service.MeasureDataService;
import gov.cms.mat.fhir.services.translate.ManageMeasureDetailMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MeasureControllerTest {
    private static final String ID = "id";
    private static final String STATUS = "status";

    @Mock
    private MeasureDataService measureDataService;
    @Mock
    private MeasureExportRepository measureExportRepo;
    @Mock
    private ManageMeasureDetailMapper manageMeasureDetailMapper;
    @Mock
    private HapiFhirServer hapiFhirServer;
    @Mock
    private ConversionResultsService conversionResultsService;
    @Mock
    private MatXmlProcessor matXmlProcessor;


    @InjectMocks
    private MeasureController measureTranslationService;


}