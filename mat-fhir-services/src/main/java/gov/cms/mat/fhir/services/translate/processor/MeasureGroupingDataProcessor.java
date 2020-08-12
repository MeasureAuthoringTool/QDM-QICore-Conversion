package gov.cms.mat.fhir.services.translate.processor;

import gov.cms.mat.fhir.services.components.mat.MatXmlConverter;
import gov.cms.mat.fhir.services.exceptions.CqlParseException;
import gov.cms.mat.fhir.services.translate.creators.FhirCreator;
import lombok.extern.slf4j.Slf4j;
import mat.client.measurepackage.MeasurePackageClauseDetail;
import mat.client.measurepackage.MeasurePackageDetail;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Expression;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
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

        String type;
        String display;
        String mappedType;

        switch (clauseDetail.getType()) {
            case "initialPopulation":
                type = "initial-population";
                display = "Initial Population";
                mappedType = clauseDetail.getCqlDefinition().getDisplay();
                break;
            case "denominator":
                type = "denominator";
                display = "Denominator";
                mappedType = clauseDetail.getCqlDefinition().getDisplay();
                break;
            case "denominatorExclusions":
                type = "denominator-exclusion";
                display = "Denominator Exclusion";
                mappedType = clauseDetail.getCqlDefinition().getDisplay();
                break;
            case "denominatorExceptions":
                type = "denominator-exception";
                display = "Denominator Exception";
                mappedType =clauseDetail.getCqlDefinition().getDisplay();
                break;
            case "numerator":
                type = "numerator";
                display = "Numerator";
                mappedType = clauseDetail.getCqlDefinition().getDisplay();
                break;
            case "numeratorExclusions":
                type = "numerator-exclusion";
                display = "Numerator Exclusion";
                mappedType = clauseDetail.getCqlDefinition().getDisplay();
                break;
            case "measurePopulation":
                type = "measure-population";
                display = "Measure Population";
                mappedType = clauseDetail.getCqlDefinition().getDisplay();
                break;
            case "measurePopulationExclusions":
                type = "measure-population-exclusion";
                display = "Measure Population Exclusion";
                mappedType = clauseDetail.getCqlDefinition().getDisplay();
                break;
            case "measureObservation":
                type = "measure-observation";
                display = "Measure Observation";

                //The value for this is definitely wrong.
                // Need to figure that out some day.
                // We have no way to set this in MAT atm.
                component.addExtension("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-criteriaReference"
                        ,new StringType("criteriaReference"));

                component.addExtension("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-aggregateMethod",
                        new CodeType(StringUtils.lowerCase(clauseDetail.getAggregateFunction().getDisplay())));

                mappedType = clauseDetail.getAggregateFunction().getCqlFunction().getDisplay();
                break;
            default:
                log.debug("Invalid MeasurePackageClauseDetail: " +
                        "could not map type: " + clauseDetail.getType());
                throw new CqlParseException("Invalid MeasurePackageClauseDetail: " +
                        "could not map type: " + clauseDetail.getType());
        }

        return component
                .setCriteria(new Expression()
                        .setLanguage("text/cql.identifier")
                        .setExpression(mappedType))
                .setCode(buildCodeableConcept(type, SYSTEM, display));
    }
}
