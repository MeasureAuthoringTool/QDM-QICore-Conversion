package gov.cms.mat.fhir.services.service;

import java.util.Collections;
import java.util.List;

import org.hl7.fhir.r4.model.ValueSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.components.xml.MatXmlProcessor;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.ValueSetConversionException;
import gov.cms.mat.fhir.services.translate.ValueSetMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValueSetServiceTest {
    private static final String MEASURE_ID = "measureId";
    private static final String VSAC_GRANTING_TICKET = "vsacGrantingTicket";

    @Mock
    private MeasureExportDataService measureExportDataService;
    @Mock
    private ValueSetMapper valueSetMapper;
    @Mock
    private MatXmlProcessor matXmlProcessor;
    @Mock
    private ConversionResultsService conversionResultsService;
    @Mock
    private MeasureDataService measureDataService;

    @InjectMocks
    private ValueSetService valueSetService;

    @Test
    void count() {
        when(valueSetMapper.count()).thenReturn(Integer.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, valueSetService.count());

        verify(valueSetMapper).count();
        verifyNoInteractions(conversionResultsService);
    }

    @Test
    void deleteAll() {
        when(valueSetMapper.deleteAll()).thenReturn(Integer.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, valueSetService.deleteAll());

        verify(valueSetMapper).deleteAll();
    }

    @Test
    void findValueSets_XmlIsNull() {
        Measure matMeasure = new Measure();
        matMeasure.setId(MEASURE_ID);
        when(measureDataService.findOneValid(MEASURE_ID)).thenReturn(matMeasure);

        when(matXmlProcessor.getXmlById(MEASURE_ID, XmlSource.SIMPLE)).thenReturn(null);

        assertThrows(ValueSetConversionException.class,
                () -> valueSetService.findValueSetsByMeasureId(XmlSource.SIMPLE, MEASURE_ID, ConversionType.CONVERSION, VSAC_GRANTING_TICKET));


        verify(measureDataService).findOneValid(MEASURE_ID);
        verify(matXmlProcessor).getXmlById(MEASURE_ID, XmlSource.SIMPLE);
    }

    @Test
    void findValueSets_HaveXml() {
        String xml = "</xml>";
        when(matXmlProcessor.getXmlById(MEASURE_ID, XmlSource.SIMPLE)).thenReturn(xml.getBytes());

        Measure matMeasure = new Measure();
        matMeasure.setId(MEASURE_ID);
        when(measureDataService.findOneValid(MEASURE_ID)).thenReturn(matMeasure);

        ValueSet valueSet = new ValueSet();
        when(valueSetMapper.translateToFhir(xml, VSAC_GRANTING_TICKET)).thenReturn(Collections.singletonList(valueSet));

        List<ValueSet> valueSets = valueSetService.findValueSetsByMeasureId(XmlSource.SIMPLE, MEASURE_ID, ConversionType.CONVERSION, VSAC_GRANTING_TICKET);

        assertEquals(1, valueSets.size());
        assertEquals(valueSet, valueSets.get(0));

        verify(measureDataService).findOneValid(MEASURE_ID);
        verify(matXmlProcessor).getXmlById(MEASURE_ID, XmlSource.SIMPLE);
    }

}