package gov.cms.mat.fhir.services.translate.processor;

import gov.cms.mat.fhir.services.components.mat.MatXmlConverter;
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


        String type = clauseDetail.getType();
        String display = StringUtils.capitalize(clauseDetail.getType());


        switch (clauseDetail.getType()) {
            case "initialPopulation":
                type = "initial-population";
                display = "Initial Population";
                break;

            case "denominatorExclusion":
                type = "denominator-exclusion";
                display = "Denominator Exclusion";
                break;

            case "numeratorExclusion":
                type = "numerator-exclusion";
                display = "Numerator Exclusion";
                break;

            case "measurePopulation":
                type = "measure-population";
                display = "Measure Population";
                break;

            case "measureObservation":
                type = "measure-observation";
                display = "Measure Observation";


                Extension criteriaReferenceExtension = new Extension();
                criteriaReferenceExtension.setUrl("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-criteriaReference");
                criteriaReferenceExtension.setValue(new StringType("criteriaReference"));
                component.addExtension(criteriaReferenceExtension);

                Extension aggregateMethodExtension = new Extension();
                aggregateMethodExtension.setUrl("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-aggregateMethod");
                aggregateMethodExtension.setValue(new CodeType("aggregateMethod"));
                component.addExtension(aggregateMethodExtension);

                break;

            case "denominatorException":
                type = "denominatorException";
                display = "Denominator Exception";
                break;
            default:
                log.debug("Did not set type and display in switch");
                break;
        }

        return component
                .setCriteria(buildExpression(clauseDetail.getDisplayName()))
                .setCode(buildCodeableConcept(type, SYSTEM, display));


    }

    private Expression buildExpression(String displayName) {
        return new Expression()
                .setLanguage("text/cql.identifier")
                .setExpression(displayName);
    }
}
