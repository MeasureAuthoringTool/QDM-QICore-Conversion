package gov.cms.mat.fhir.services.components.xml;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.commons.model.MeasureXml;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import gov.cms.mat.fhir.services.repository.MeasureRepository;
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
    private static final String ID = "id";

    @Mock
    private MeasureXmlRepository measureXmlRepository;
    @Mock
    private MeasureExportRepository measureExportRepo;
    @Mock
    private MeasureRepository measureRepository;

    @InjectMocks
    private MatXmlProcessor matXmlProcessor;

    private Measure measure;

    @BeforeEach
    void setUP() {
        measure = new Measure();
    }

    @Test
    void getXmlById_Empty() {
        when(measureRepository.findById(ID)).thenReturn(Optional.empty());
        assertNull(matXmlProcessor.getXmlById(ID, XmlSource.SIMPLE));

        verify(measureRepository).findById(ID);
        verifyNoInteractions(measureXmlRepository, measureExportRepo);
    }

    @Test
    void getXmlById_Found_Simple() {
        when(measureRepository.findById(ID)).thenReturn(Optional.of(measure));

        MeasureExport measureExport = new MeasureExport();
        measureExport.setSimpleXml(XML_BYTES);

        when(measureExportRepo.findByMeasureId(measure)).thenReturn(Optional.of(measureExport));

        byte[] xmlBytes = matXmlProcessor.getXmlById(ID, XmlSource.SIMPLE);
        assertEquals(XML_BYTES, xmlBytes);

        verify(measureRepository).findById(ID);
        verify(measureExportRepo).findByMeasureId(measure);
        verifyNoInteractions(measureXmlRepository);
    }

    @Test
    void getXmlById_Found_Measure() {
        when(measureRepository.findById(ID)).thenReturn(Optional.of(measure));

        MeasureXml measureXml = new MeasureXml();
        measureXml.setMeasureXml(XML_BYTES);

        when(measureXmlRepository.findByMeasureId(measure)).thenReturn(Optional.of(measureXml));

        byte[] xmlBytes = matXmlProcessor.getXmlById(ID, XmlSource.MEASURE);
        assertEquals(XML_BYTES, xmlBytes);

        verify(measureRepository).findById(ID);
        verify(measureXmlRepository).findByMeasureId(measure);
        verifyNoInteractions(measureExportRepo);
    }

    @Test
    void getSimpleXml_Empty() {
        when(measureExportRepo.findByMeasureId(measure)).thenReturn(Optional.empty());

        assertNull(matXmlProcessor.getSimpleXml(measure));

        verify(measureExportRepo).findByMeasureId(measure);
        verifyNoInteractions(measureXmlRepository, measureRepository);
    }

    @Test
    void getSimpleXml_Found() {
        MeasureExport measureExport = new MeasureExport();
        measureExport.setSimpleXml(XML_BYTES);

        when(measureExportRepo.findByMeasureId(measure)).thenReturn(Optional.of(measureExport));

        byte[] xmlBytes = matXmlProcessor.getSimpleXml(measure);
        assertEquals(XML_BYTES, xmlBytes);

        verify(measureExportRepo).findByMeasureId(measure);
        verifyNoInteractions(measureXmlRepository, measureRepository);
    }

    @Test
    void getMeasureXml_Empty() {
        when(measureXmlRepository.findByMeasureId(measure)).thenReturn(Optional.empty());

        assertNull(matXmlProcessor.getMeasureXml(measure));

        verify(measureXmlRepository).findByMeasureId(measure);
        verifyNoMoreInteractions(measureExportRepo, measureRepository);
    }

    @Test
    void getMeasureXml_Found() {
        MeasureXml measureXml = new MeasureXml();
        measureXml.setMeasureXml(XML_BYTES);

        when(measureXmlRepository.findByMeasureId(measure)).thenReturn(Optional.of(measureXml));

        byte[] xmlBytes = matXmlProcessor.getMeasureXml(measure);
        assertEquals(XML_BYTES, xmlBytes);

        verify(measureXmlRepository).findByMeasureId(measure);
        verifyNoMoreInteractions(measureExportRepo, measureRepository);
    }
}