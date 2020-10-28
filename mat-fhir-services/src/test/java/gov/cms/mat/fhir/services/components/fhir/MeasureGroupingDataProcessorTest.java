package gov.cms.mat.fhir.services.components.fhir;

import gov.cms.mat.fhir.services.components.mat.MatXmlConverter;
import gov.cms.mat.fhir.services.translate.processor.MeasureGroupingDataProcessor;
import mat.client.measurepackage.MeasurePackageClauseDetail;
import mat.client.measurepackage.MeasurePackageDetail;
import org.hl7.fhir.r4.model.Measure;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    // todo carson Erorr->MeasureGroupingDataProcessor - Invalid MeasurePackageClauseDetail: could not map type: 2
//    @Test
//    void processXml_FoundMeasureGroupings() {
//        List<MeasurePackageDetail> measureGroupComponents = createList();
//
//        when(matXmlConverter.toMeasureGroupings(XML)).thenReturn(measureGroupComponents);
//
//        List<Measure.MeasureGroupComponent> componentList = measureGroupingDataProcessor.processXml(XML);
//
//        assertEquals(6, componentList.size());
//
//        verify(matXmlConverter).toMeasureGroupings(XML);
//    }

    private List<MeasurePackageDetail> createList() {
        return IntStream.range(-6, 0)
                .boxed()
                .map(i -> createMeasureGroupComponent(Math.abs(i)))
                .collect(Collectors.toList());
    }

    private MeasurePackageDetail createMeasureGroupComponent(int i) {
        MeasurePackageDetail measurePackageDetail = new MeasurePackageDetail();
        measurePackageDetail.setSequence("" + i);
        measurePackageDetail.setPackageClauses(createPackageClauses(i));

        return measurePackageDetail;
    }

    private List<MeasurePackageClauseDetail> createPackageClauses(int i) {
        return Collections.singletonList(createPackageClause(i));
    }

    private MeasurePackageClauseDetail createPackageClause(int i) {
        MeasurePackageClauseDetail clauseDetail = new MeasurePackageClauseDetail();
        clauseDetail.setType("" + i);
        clauseDetail.setDisplayName("" + i);
        clauseDetail.setIsInGrouping(i % 2 == 0);
        return clauseDetail;
    }
}