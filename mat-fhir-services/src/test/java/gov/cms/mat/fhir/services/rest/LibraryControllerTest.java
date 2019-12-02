package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.commons.objects.TranslationOutcome;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.repository.CqlLibraryExportRepository;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import gov.cms.mat.fhir.services.repository.MeasureRepository;
import gov.cms.mat.fhir.services.translate.ManageMeasureDetailMapper;
import mat.client.measure.ManageCompositeMeasureDetailModel;
import mat.client.measure.PeriodModel;
import mat.model.MeasureType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryControllerTest {
    private static final String ID = "id";
    private static final String STATUS = "status";

    @Mock
    private MeasureRepository measureRepo;
    @Mock
    private HapiFhirServer hapiFhirServer;
    @Mock
    private CqlLibraryRepository cqlLibraryRepo;
    @Mock
    private CqlLibraryExportRepository cqlLibraryExportRepo;
    @Mock
    private ConversionResultsService conversionResultsService;


    @InjectMocks
    private LibraryController libraryController;



    @Test
    void translateLibraryByMeasureId() {
    }

    @Test
    void translateAllLibraries() {
    }

    @Test
    void removeAllLibraries() {
    }
}