package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.rest.cql.ConversionType;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.components.xml.MatXmlProcessor;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.translate.ValueSetMapper;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValueSetServiceTest {
    private static final String MEASURE_ID = "measureId";

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
        String measureId = "measureId";

        when(matXmlProcessor.getXmlById(measureId, XmlSource.SIMPLE)).thenReturn(null);

        assertTrue(valueSetService.findValueSets(XmlSource.SIMPLE, measureId, ConversionType.CONVERSION).isEmpty());

        verify(measureDataService).findOneValid(measureId);
        verify(matXmlProcessor).getXmlById(measureId, XmlSource.SIMPLE);
    }

    @Test
    void findValueSets_HaveXml() {
        String xml = "</xml>";
        when(matXmlProcessor.getXmlById(MEASURE_ID, XmlSource.SIMPLE)).thenReturn(xml.getBytes());

        ValueSet valueSet = new ValueSet();

        when(valueSetMapper.translateToFhir(xml, ConversionType.CONVERSION)).thenReturn(Collections.singletonList(valueSet));

        List<ValueSet> valueSets = valueSetService.findValueSets(XmlSource.SIMPLE, MEASURE_ID, ConversionType.CONVERSION);

        assertEquals(1, valueSets.size());
        assertEquals(valueSet, valueSets.get(0));

        verify(measureDataService).findOneValid(MEASURE_ID);
        verify(matXmlProcessor).getXmlById(MEASURE_ID, XmlSource.SIMPLE);
    }

    @Test
    void translateAll() {
    }
}