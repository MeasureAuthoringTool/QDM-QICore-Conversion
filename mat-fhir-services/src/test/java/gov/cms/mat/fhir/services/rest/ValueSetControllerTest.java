package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.commons.objects.TranslationOutcome;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import gov.cms.mat.fhir.services.summary.MeasureVersionExportId;
import gov.cms.mat.fhir.services.translate.ValueSetMapper;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static gov.cms.mat.fhir.services.rest.ValueSetController.ALLOWED_VERSIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValueSetControllerTest {
    @Mock
    private MeasureExportRepository measureExportRepository;
    @Mock
    private ValueSetMapper valueSetMapper;
    @Mock
    private ConversionResultsService conversionResultsService;

    @InjectMocks
    private ValueSetController valueSetController;

    @Test
    void translateAll_NoneToTranslate() {
        TranslationOutcome translationOutcome = valueSetController.translateAll();
        assertTrue(translationOutcome.getMessage()
                .startsWith("Read 0 Measure Export objects converted 0 Value sets to fhir in"));

        verify(valueSetMapper, times(2)).count();
        verify(measureExportRepository).getAllExportIdsAndVersion(ALLOWED_VERSIONS);
        verify(measureExportRepository, never()).findById(anyString());
    }

    @Test
    void translateAll_TranslateRecord() {
        String idGood = "good";
        String idBad = "bad";
        String xml = "XML";

        List<MeasureVersionExportId> idsAndVersion =
                Arrays.asList(new MeasureVersionExportId(idGood, ALLOWED_VERSIONS.get(0)),
                        new MeasureVersionExportId(idBad, ALLOWED_VERSIONS.get(0)));

        MeasureExport measureExport = new MeasureExport();
        measureExport.setSimpleXml(xml.getBytes());

        when(valueSetMapper.translateToFhir(xml)).thenReturn(Collections.singletonList(new ValueSet()));

        when(measureExportRepository.getAllExportIdsAndVersion(ALLOWED_VERSIONS)).thenReturn(idsAndVersion);

        when(measureExportRepository.findById(idGood)).thenReturn(Optional.of(measureExport));
        when(measureExportRepository.findById(idBad)).thenReturn(Optional.empty());

        when(valueSetMapper.count())
                .thenReturn(0)
                .thenReturn(1);

        TranslationOutcome translationOutcome = valueSetController.translateAll();

        assertTrue(translationOutcome.getMessage()
                .startsWith("Read 2 Measure Export objects converted 1 Value sets to fhir in"));

        verify(valueSetMapper, times(2)).count();
        verify(measureExportRepository).getAllExportIdsAndVersion(ALLOWED_VERSIONS);
        verify(measureExportRepository, times(2)).findById(anyString());
        verify(valueSetMapper).translateToFhir(xml);

    }

    @Test
    void countValueSets() {
        when(valueSetMapper.count()).thenReturn(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, valueSetController.countValueSets());
    }

    @Test
    void deleteValueSets() {
        when(valueSetMapper.deleteAll()).thenReturn(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, valueSetController.deleteValueSets());
    }
}