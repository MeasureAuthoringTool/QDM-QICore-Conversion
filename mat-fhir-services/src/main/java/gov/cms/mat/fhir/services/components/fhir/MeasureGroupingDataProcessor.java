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
    private static final String LANGUAGE = "text/cql";
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
                .map(this::processDetails2)
                .collect(Collectors.toList());
    }

    private Measure.MeasureGroupComponent processDetails2(MeasurePackageDetail m) {
        Measure.MeasureGroupComponent component = new Measure.MeasureGroupComponent();

        if (CollectionUtils.isNotEmpty(m.getPackageClauses())) {
            component.setPopulation(createPopulations(m.getPackageClauses()));
        }

        return component;
    }

    private List<Measure.MeasureGroupPopulationComponent> createPopulations(List<MeasurePackageClauseDetail> packageClauses) {
        return packageClauses.stream()
                .filter(MeasurePackageClauseDetail::isInGrouping) //todo this not mapping
                .map(this::createPopulation)
                .collect(Collectors.toList());
    }

    private Measure.MeasureGroupPopulationComponent createPopulation(MeasurePackageClauseDetail c) {
        Measure.MeasureGroupPopulationComponent component = new Measure.MeasureGroupPopulationComponent();

        Expression value = new Expression();
        value.setLanguage(Expression.ExpressionLanguage.TEXT_CQL);
        value.setExpression(c.getDisplayName());
        component.setCode(buildCodeableConcept(c.getType(), SYSTEM, c.getDisplayName()));
        return component;
    }

    private Expression buildCqlExpression(String expression) {
        return new Expression()
                .setExpression(expression)
                .setLanguage(Expression.ExpressionLanguage.TEXT_CQL);
    }

}
