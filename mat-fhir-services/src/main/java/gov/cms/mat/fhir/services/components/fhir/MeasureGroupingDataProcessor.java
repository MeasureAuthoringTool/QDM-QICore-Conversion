package gov.cms.mat.fhir.services.components.fhir;

import gov.cms.mat.fhir.services.components.mat.MatXmlConverter;
import gov.cms.mat.fhir.services.translate.creators.FhirCreator;
import mat.client.measurepackage.MeasurePackageClauseDetail;
import mat.client.measurepackage.MeasurePackageDetail;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.r4.model.Expression;
import org.hl7.fhir.r4.model.Measure;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MeasureGroupingDataProcessor implements FhirCreator {
    private static final String SYSTEM = "http://terminology.hl7.org/CodeSystem/measure-population";
    private final MatXmlConverter matXmlConverter;

    public MeasureGroupingDataProcessor(MatXmlConverter matXmlConverter) {
        this.matXmlConverter = matXmlConverter;
    }

    public List<Measure.MeasureGroupComponent> processXml(String xml) {
        List<MeasurePackageDetail> list = matXmlConverter.toMeasureGroupings(xml);

        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        } else {
            list.sort(MeasurePackageDetail::compareTo);

            return processDetails(list);
        }
    }

    private List<Measure.MeasureGroupComponent> processDetails(List<MeasurePackageDetail> list) {
        return list.stream()
                .map(this::createMeasureGroupComponent)
                .collect(Collectors.toList());
    }

    private Measure.MeasureGroupComponent createMeasureGroupComponent(MeasurePackageDetail measurePackageDetail) {
        Measure.MeasureGroupComponent component = new Measure.MeasureGroupComponent();

        if (CollectionUtils.isNotEmpty(measurePackageDetail.getPackageClauses())) {
            component.setPopulation(createPopulations(measurePackageDetail.getPackageClauses()));
        }

        return component;
    }

    private List<Measure.MeasureGroupPopulationComponent> createPopulations(List<MeasurePackageClauseDetail> packageClauses) {
        return packageClauses.stream()
                .filter(MeasurePackageClauseDetail::isInGrouping)
                .map(this::createPopulation)
                .collect(Collectors.toList());
    }

    private Measure.MeasureGroupPopulationComponent createPopulation(MeasurePackageClauseDetail clauseDetail) {
        Measure.MeasureGroupPopulationComponent component = new Measure.MeasureGroupPopulationComponent();

        Expression value = new Expression();
        value.setLanguage(Expression.ExpressionLanguage.TEXT_CQL);
        value.setExpression(clauseDetail.getDisplayName());
        component.setCode(buildCodeableConcept(clauseDetail.getType(), SYSTEM, clauseDetail.getDisplayName()));
        return component;
    }


}
