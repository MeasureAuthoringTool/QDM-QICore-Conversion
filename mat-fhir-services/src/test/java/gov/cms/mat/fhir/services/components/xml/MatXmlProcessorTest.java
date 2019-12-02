package gov.cms.mat.fhir.services.components.xml;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.commons.model.MeasureXml;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import gov.cms.mat.fhir.services.repository.MeasureXmlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatXmlProcessorTest {
    private static final byte[] XML_BYTES = "<xml>I Am XML</xml>".getBytes();
    @Mock
    private MeasureXmlRepository measureXmlRepository;
    @Mock
    private MeasureExportRepository measureExportRepo;
    @InjectMocks
    private MatXmlProcessor matXmlProcessor;
    private Measure measure;

    @BeforeEach
    void setUP() {
        measure = new Measure();
    }

    @Test
    void getSimpleXml_Empty() {
        when(measureExportRepo.findByMeasureId(measure)).thenReturn(Optional.empty());

        assertNull(matXmlProcessor.getSimpleXml(measure));

        verify(measureExportRepo).findByMeasureId(measure);
        verifyNoInteractions(measureXmlRepository);
    }

    @Test
    void getSimpleXml_Found() {
        MeasureExport measureExport = new MeasureExport();
        measureExport.setSimpleXml(XML_BYTES);

        when(measureExportRepo.findByMeasureId(measure)).thenReturn(Optional.of(measureExport));

        byte[] xmlBytes = matXmlProcessor.getSimpleXml(measure);
        assertEquals(XML_BYTES, xmlBytes);

        verify(measureExportRepo).findByMeasureId(measure);
        verifyNoInteractions(measureXmlRepository);
    }

    @Test
    void getMeasureXml_Empty() {
        when(measureXmlRepository.findByMeasureId(measure)).thenReturn(Optional.empty());

        assertNull(matXmlProcessor.getMeasureXml(measure));

        verify(measureXmlRepository).findByMeasureId(measure);
        verifyNoMoreInteractions(measureExportRepo);
    }

    @Test
    void getMeasureXml_Found() {
        MeasureXml measureXml = new MeasureXml();
        measureXml.setMeasureXml(XML_BYTES);

        when(measureXmlRepository.findByMeasureId(measure)).thenReturn(Optional.of(measureXml));

        byte[] xmlBytes = matXmlProcessor.getMeasureXml(measure);
        assertEquals(XML_BYTES, xmlBytes);

        verify(measureXmlRepository).findByMeasureId(measure);
        verifyNoMoreInteractions(measureExportRepo);
    }
}