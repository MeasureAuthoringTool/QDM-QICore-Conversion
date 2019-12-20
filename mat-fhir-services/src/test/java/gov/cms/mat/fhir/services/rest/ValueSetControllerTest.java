package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.objects.TranslationOutcome;
import gov.cms.mat.fhir.services.components.mongo.ConversionType;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.ValueSetConversionException;
import gov.cms.mat.fhir.services.service.ValueSetService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValueSetControllerTest {
    private static final List<String> ALLOWED_VERSIONS = Arrays.asList("v5.5", "v5.6", "v5.7", "v5.8");

    @Mock
    private ValueSetService valueSetService;

    @InjectMocks
    private ValueSetController valueSetController;

    @Test
    void translateAll() {
        String successMessage = "success";

        when(valueSetService.translateAll(XmlSource.SIMPLE, ConversionType.CONVERSION)).thenReturn(successMessage);

        TranslationOutcome translationOutcome = valueSetController.translateAll(XmlSource.SIMPLE, ConversionType.CONVERSION);
        assertEquals(successMessage, translationOutcome.getMessage());
    }

    @Test
    void validate_NoMeasure() {
        String measureId = "measureId";

        when(valueSetService.findValueSets(XmlSource.SIMPLE, measureId, ConversionType.VALIDATION))
                .thenReturn(Collections.emptyList());

        Assertions.assertThrows(ValueSetConversionException.class, () -> {
            valueSetController.validate(XmlSource.SIMPLE, measureId);
        });
    }

    @Test
    void validate_WithMeasure() {
        String measureId = "measureId"; //todo redo when result component

//        when(valueSetService.findValueSets(XmlSource.SIMPLE, measureId, ConversionType.VALIDATION))
//                .thenReturn(Collections.emptyList());
//
//        Assertions.assertThrows(ValueSetConversionException.class, () -> {
//            valueSetController.validate(XmlSource.SIMPLE, measureId);
//        });
    }


//    @Test
//    void translateAll_TranslateRecord() {
//        String idGood = "good";
//        String idBad = "bad";
//        String xml = "XML";
//        String id = "ID";
//
//        List<MeasureVersionExportId> idsAndVersion =
//                Arrays.asList(new MeasureVersionExportId(idGood, ALLOWED_VERSIONS.get(0)),
//                        new MeasureVersionExportId(idBad, ALLOWED_VERSIONS.get(0)));
//
//        when(valueSetMapper.translateToFhir(xml, ConversionType.CONVERSION)).thenReturn(Collections.singletonList(new ValueSet()));
//
//        when(measureExportRepository.getAllExportIdsAndVersion()).thenReturn(idsAndVersion);
//
//        MeasureExport measureExport = new MeasureExport();
//        measureExport.setMeasureId(id);
//
//        when(matXmlProcessor.getXmlById(id, XmlSource.SIMPLE)).thenReturn(xml.getBytes());
//
//        when(measureExportRepository.findById(idGood)).thenReturn(Optional.of(measureExport));
//        when(measureExportRepository.findById(idBad)).thenReturn(Optional.empty());
//
//        when(valueSetMapper.count())
//                .thenReturn(0)
//                .thenReturn(1);
//
//        TranslationOutcome translationOutcome = valueSetController.translateAll(XmlSource.SIMPLE, ConversionType.CONVERSION);
//
//        assertTrue(translationOutcome.getMessage()
//                .startsWith("Read 2 Measure Export objects converted 1 Value sets to fhir in"));
//
//        verify(valueSetMapper, times(2)).count();
//        verify(measureExportRepository).getAllExportIdsAndVersion();
//        verify(measureExportRepository, times(2)).findById(anyString());
//        verify(valueSetMapper).translateToFhir(xml, ConversionType.CONVERSION);
//        verify(matXmlProcessor).getXmlById(id, XmlSource.SIMPLE);
//    }

    @Test
    void countValueSets() {
        when(valueSetService.count()).thenReturn(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, valueSetController.countValueSets());
    }

    @Test
    void deleteValueSets() {
        when(valueSetService.deleteAll()).thenReturn(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, valueSetController.deleteValueSets());
    }
}