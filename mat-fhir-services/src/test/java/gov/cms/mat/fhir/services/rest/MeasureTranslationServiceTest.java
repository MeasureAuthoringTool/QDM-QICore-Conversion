package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.commons.objects.TranslationOutcome;
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
class MeasureTranslationServiceTest {
    private static final String ID = "id";
    private static final String STATUS = "status";

    @Mock
    private MeasureRepository measureRepo;
    @Mock
    private MeasureExportRepository measureExportRepo;
    @Mock
    private ManageMeasureDetailMapper manageMeasureDetailMapper;
    @Mock
    private HapiFhirServer hapiFhirServer;
    @Mock
    private CqlLibraryRepository cqlLibraryRepo;
    @Mock
    private CqlLibraryExportRepository cqlLibraryExportRepo;

    @InjectMocks
    private MeasureTranslationService measureTranslationService;

    @Test
    void translateMeasureById_NotFoundMeasureRepo() {
        when(measureRepo.getMeasureById(ID)).thenReturn(null);

        TranslationOutcome translationOutcome = measureTranslationService.translateMeasureById(ID);

        assertTrue(translationOutcome.getMessage().contains("No SimpleXML Available"));
        assertFalse(translationOutcome.getSuccessful());

        verifyNoInteractions(measureExportRepo, manageMeasureDetailMapper, hapiFhirServer);

        verify(measureRepo).getMeasureById(ID);
    }

    @Test
    void translateMeasureById_NotFoundMeasureRep() {
        when(measureRepo.getMeasureById(ID)).thenReturn(new Measure());
        when(measureExportRepo.getMeasureExportById(ID)).thenReturn(null);

        TranslationOutcome translationOutcome = measureTranslationService.translateMeasureById(ID);

        assertTrue(translationOutcome.getMessage().contains("No SimpleXML Available"));
        assertFalse(translationOutcome.getSuccessful());

        verifyNoInteractions(hapiFhirServer);
        verify(measureRepo).getMeasureById(ID);
        verify(measureExportRepo).getMeasureExportById(ID);
    }

    @Test
    void translateMeasureById_ManageMeasureDetailMapperException() {
        Measure measure = new Measure();
        when(measureRepo.getMeasureById(ID)).thenReturn(measure);
        when(measureExportRepo.getMeasureExportById(ID)).thenReturn(new MeasureExport());

        String message = "oops";
        when(manageMeasureDetailMapper.convert(null, measure))
                .thenThrow(new IllegalArgumentException(message));

        TranslationOutcome translationOutcome = measureTranslationService.translateMeasureById(ID);

        assertTrue(translationOutcome.getMessage().contains(message));
        assertFalse(translationOutcome.getSuccessful());

        verifyNoInteractions(hapiFhirServer);
        verify(measureRepo).getMeasureById(ID);
        verify(measureExportRepo).getMeasureExportById(ID);
        verify(manageMeasureDetailMapper).convert(null, measure);
    }

    @Test
    void translateMeasureById_Success() {
        Measure measure = new Measure();
        when(measureRepo.getMeasureById(ID)).thenReturn(measure);
        when(measureExportRepo.getMeasureExportById(ID)).thenReturn(new MeasureExport());

        ManageCompositeMeasureDetailModel manageCompositeMeasureDetailModel = createManageCompositeMeasureDetailModel();
        when(manageMeasureDetailMapper.convert(null, measure))
                .thenReturn(manageCompositeMeasureDetailModel);

        when(hapiFhirServer.createAndExecuteBundle(any(Resource.class))).thenReturn(new Bundle());

        TranslationOutcome translationOutcome = measureTranslationService.translateMeasureById(ID);

        assertTrue(translationOutcome.getMessage().isEmpty());
        assertTrue(translationOutcome.getSuccessful());

        verify(measureRepo).getMeasureById(ID);
        verify(measureExportRepo).getMeasureExportById(ID);
        verify(manageMeasureDetailMapper).convert(null, measure);
        verify(hapiFhirServer).createAndExecuteBundle(any(Resource.class));
    }


    @Test
    void translateMeasuresByStatus_NotFoundInDb() {
        when(measureRepo.getMeasuresByStatus(STATUS)).thenReturn(Collections.emptyList());

        List<TranslationOutcome> translationOutcomes = measureTranslationService.translateMeasuresByStatus(STATUS);

        assertTrue(translationOutcomes.isEmpty());

        verify(measureRepo).getMeasuresByStatus(STATUS);
    }

    @Test
    void translateMeasuresByStatus_Exception() {
        when(measureRepo.getMeasuresByStatus(STATUS)).thenThrow(new IllegalArgumentException("oops"));

        List<TranslationOutcome> translationOutcomes = measureTranslationService.translateMeasuresByStatus(STATUS);

        assertEquals(1, translationOutcomes.size());
        assertFalse(translationOutcomes.get(0).getSuccessful());

        verify(measureRepo).getMeasuresByStatus(STATUS);
    }

    @Test
    void translateMeasuresByStatus_FoundInDb() {
        Measure measure = new Measure();
        measure.setId(ID);
        when(measureRepo.getMeasuresByStatus(STATUS)).thenReturn(Collections.singletonList(measure));
        when(measureRepo.getMeasureById(ID)).thenReturn(measure);
        when(measureExportRepo.getMeasureExportById(ID)).thenReturn(new MeasureExport());
        when(manageMeasureDetailMapper.convert(null, measure))
                .thenReturn(createManageCompositeMeasureDetailModel());

        List<TranslationOutcome> translationOutcomes = measureTranslationService.translateMeasuresByStatus(STATUS);

        assertEquals(1, translationOutcomes.size());
        assertTrue(translationOutcomes.get(0).getSuccessful());

        verify(measureRepo).getMeasuresByStatus(STATUS);
        verify(measureRepo).getMeasureById(ID);
        verify(measureExportRepo).getMeasureExportById(ID);
        verify(manageMeasureDetailMapper).convert(null, measure);
    }

    @Test
    void translateAllMeasures_NotFoundInDb() {
        when(measureRepo.findAll()).thenReturn(Collections.emptyList());

        List<TranslationOutcome> translationOutcomes = measureTranslationService.translateAllMeasures();
        assertTrue(translationOutcomes.isEmpty());

        verify(measureRepo).findAll();
    }

    @Test
    void translateAllMeasures_Exception() {
        when(measureRepo.findAll()).thenThrow(new IllegalArgumentException("bad things happened"));

        List<TranslationOutcome> translationOutcomes = measureTranslationService.translateAllMeasures();
        assertEquals(1, translationOutcomes.size());
        assertFalse(translationOutcomes.get(0).getSuccessful());

        verify(measureRepo).findAll();
    }

    @Test
    void translateAllMeasures_FoundInDb() {
        Measure measure = new Measure();
        measure.setId(ID);
        measure.setReleaseVersion("v5.5");

        when(measureRepo.findAll()).thenReturn(Collections.singletonList(measure));
        when(measureRepo.getMeasureById(ID)).thenReturn(measure);
        when(measureExportRepo.getMeasureExportById(ID)).thenReturn(new MeasureExport());
        when(manageMeasureDetailMapper.convert(null, measure))
                .thenReturn(createManageCompositeMeasureDetailModel());


        List<TranslationOutcome> translationOutcomes = measureTranslationService.translateAllMeasures();

        assertEquals(1, translationOutcomes.size());
        assertTrue(translationOutcomes.get(0).getSuccessful());

        verify(measureRepo).findAll();
        verify(measureRepo).getMeasureById(ID);
        verify(measureExportRepo).getMeasureExportById(ID);
        verify(manageMeasureDetailMapper).convert(null, measure);
    }

    @Test
    void removeAllMeasures() {
    }

    @Test
    void translateLibraryByMeasureId() {
    }

    @Test
    void translateAllLibraries() {
    }

    @Test
    void removeAllLibraries() {
    }

    private ManageCompositeMeasureDetailModel createManageCompositeMeasureDetailModel() {
        ManageCompositeMeasureDetailModel manageCompositeMeasureDetailModel = new ManageCompositeMeasureDetailModel();
        manageCompositeMeasureDetailModel.setId(ID);
        manageCompositeMeasureDetailModel.setEndorseByNQF(true);
        manageCompositeMeasureDetailModel.setNqfId(ID);
        manageCompositeMeasureDetailModel.setPeriodModel(createPeriodModel());
        manageCompositeMeasureDetailModel.setReferencesList(Collections.singletonList("reference"));
        manageCompositeMeasureDetailModel.setMeasureTypeSelectedList(createMeasureTypeList());
        return manageCompositeMeasureDetailModel;
    }


    private List<MeasureType> createMeasureTypeList() {
        return Arrays.asList(createMeasureType("COMPOSITE"),
                createMeasureType("INTERM-OM"),
                createMeasureType("OUTCOME"),
                createMeasureType("PRO-PM"),
                createMeasureType("STRUCTURE"),
                createMeasureType("RESOURCE"),
                createMeasureType("APPROPRIATE"),
                createMeasureType("EFFICIENCY"),
                createMeasureType("PROCESS"),
                createMeasureType("unknown"));
    }

    private MeasureType createMeasureType(String abbr) {
        MeasureType measureType = new MeasureType();
        measureType.setAbbrName(abbr);
        return measureType;
    }

    private PeriodModel createPeriodModel() {
        PeriodModel periodModel = new PeriodModel();
        periodModel.setStartDate(new Date().toString());
        periodModel.setStopDate(new Date().toString());
        return periodModel;
    }
}