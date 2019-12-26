package gov.cms.mat.fhir.services.components.fhir;

import gov.cms.mat.fhir.services.components.mat.MatXmlConverter;
import org.hl7.fhir.r4.model.Measure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeasureGroupingDataProcessorTest {
    private final static String XML = "</xml>";

    @Mock
    private MatXmlConverter matXmlConverter;

    @InjectMocks
    private MeasureGroupingDataProcessor measureGroupingDataProcessor;

    @Test
    void processXml_NoMeasureGroupings() {
        when(matXmlConverter.toMeasureGroupings(XML)).thenReturn(Collections.emptyList());

        assertTrue(measureGroupingDataProcessor.processXml(XML).isEmpty());

        verify(matXmlConverter).toMeasureGroupings(XML);
    }

    @Test
    void processXml_FoundMeasureGroupings() {
        List<Measure.MeasureGroupComponent> measureGroupComponents = createList();

        when(matXmlConverter.toMeasureGroupings(XML)).thenReturn(Collections.emptyList());

        assertTrue(measureGroupingDataProcessor.processXml(XML).isEmpty());

        verify(matXmlConverter).toMeasureGroupings(XML);
    }

    private List<Measure.MeasureGroupComponent> createList() {
        return IntStream.range(-3, 0)
                .boxed()
                .map(i -> createMeasureGroupComponent(Math.abs(i)))
                .collect(Collectors.toList());
    }

    private Measure.MeasureGroupComponent createMeasureGroupComponent(int i) {
//        Measure.MeasureGroupComponent measureGroupComponent = new Measure.MeasureGroupComponent();
//        measureGroupComponent.setPa

        return null;

    }
}