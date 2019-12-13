package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.commons.objects.TranslationOutcome;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.components.mongo.ConversionType;
import gov.cms.mat.fhir.services.components.xml.MatXmlProcessor;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.service.MeasureExportService;
import gov.cms.mat.fhir.services.summary.MeasureVersionExportId;
import gov.cms.mat.fhir.services.translate.ValueSetMapper;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValueSetControllerTest {
    private static final List<String> ALLOWED_VERSIONS = Arrays.asList("v5.5", "v5.6", "v5.7", "v5.8");


    @Mock
    private MeasureExportService measureExportRepository;
    @Mock
    private ValueSetMapper valueSetMapper;
    @Mock
    private ConversionResultsService conversionResultsService;
    @Mock
    private MatXmlProcessor matXmlProcessor;

    @InjectMocks
    private ValueSetController valueSetController;

    @BeforeEach
    public void setUp() {
        //  ReflectionTestUtils.setField(valueSetController, "allowedVersions", ALLOWED_VERSIONS);
    }

    @Test
    void translateAll_NoneToTranslate() {
        TranslationOutcome translationOutcome = valueSetController.translateAll(null, ConversionType.CONVERSION);
        assertTrue(translationOutcome.getMessage()
                .startsWith("Read 0 Measure Export objects converted 0 Value sets to fhir in"));

        verify(valueSetMapper, times(2)).count();
        verify(measureExportRepository).getAllExportIdsAndVersion();
        verify(measureExportRepository, never()).findById(anyString());

        verifyNoInteractions(conversionResultsService);
    }

    @Test
    void translateAll_TranslateRecord() {
        String idGood = "good";
        String idBad = "bad";
        String xml = "XML";
        String id = "ID";

        List<MeasureVersionExportId> idsAndVersion =
                Arrays.asList(new MeasureVersionExportId(idGood, ALLOWED_VERSIONS.get(0)),
                        new MeasureVersionExportId(idBad, ALLOWED_VERSIONS.get(0)));

        when(valueSetMapper.translateToFhir(xml, ConversionType.CONVERSION)).thenReturn(Collections.singletonList(new ValueSet()));

        when(measureExportRepository.getAllExportIdsAndVersion()).thenReturn(idsAndVersion);

        MeasureExport measureExport = new MeasureExport();
        measureExport.setMeasureId(id);

        when(matXmlProcessor.getXmlById(id, XmlSource.SIMPLE)).thenReturn(xml.getBytes());

        when(measureExportRepository.findById(idGood)).thenReturn(Optional.of(measureExport));
        when(measureExportRepository.findById(idBad)).thenReturn(Optional.empty());

        when(valueSetMapper.count())
                .thenReturn(0)
                .thenReturn(1);

        TranslationOutcome translationOutcome = valueSetController.translateAll(XmlSource.SIMPLE, ConversionType.CONVERSION);

        assertTrue(translationOutcome.getMessage()
                .startsWith("Read 2 Measure Export objects converted 1 Value sets to fhir in"));

        verify(valueSetMapper, times(2)).count();
        verify(measureExportRepository).getAllExportIdsAndVersion();
        verify(measureExportRepository, times(2)).findById(anyString());
        verify(valueSetMapper).translateToFhir(xml, ConversionType.CONVERSION);
        verify(matXmlProcessor).getXmlById(id, XmlSource.SIMPLE);
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