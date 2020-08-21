package gov.cms.mat.fhir.services.translate.processor;

import gov.cms.mat.fhir.services.components.mat.MatXmlConverter;
import gov.cms.mat.fhir.services.components.reporting.ConversionReporter;
import gov.cms.mat.fhir.services.translate.creators.FhirCreator;
import mat.model.RiskAdjustmentDTO;
import mat.model.cql.CQLDefinition;
import mat.model.cql.CQLDefinitionsWrapper;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Expression;
import org.hl7.fhir.r4.model.Measure;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SupplementalDataProcessor implements FhirCreator {
    public static final String MEASURE_DATA_USAGE = "http://terminology.hl7.org/CodeSystem/measure-data-usage";

    private final MatXmlConverter matXmlConverter;

    public SupplementalDataProcessor(MatXmlConverter matXmlConverter) {
        this.matXmlConverter = matXmlConverter;
    }

    public List<Measure.MeasureSupplementalDataComponent> processXml(String xml) {
        List<Measure.MeasureSupplementalDataComponent> result = new ArrayList<>();
        CQLDefinitionsWrapper supplementals = matXmlConverter.toCQLDefinitionsSupplementalData(xml);
        CQLDefinitionsWrapper riskAdjs =matXmlConverter.toCQLDefinitionsRiskAdjustments(xml);

        if (CollectionUtils.isEmpty(supplementals.getCqlDefinitions())) {
            ConversionReporter.setMeasureResult("MAT.supplementalData",
                    "Measure.supplementalData",
                    "No SupplementalData");
        } else {
            result.addAll(processSupplementalWrapper(supplementals.getCqlDefinitions()));
        }

        if (CollectionUtils.isEmpty(riskAdjs.getRiskAdjVarDTOList())) {
            ConversionReporter.setMeasureResult("MAT.riskAdjustments",
                    "Measure.riskAdjustments",
                    "No Risk Adjustments");
        } else {
            result.addAll(processRiskAdjWrapper(riskAdjs.getRiskAdjVarDTOList()));
        }

        return result;
    }

    private List<Measure.MeasureSupplementalDataComponent> processSupplementalWrapper(List<CQLDefinition> definitions) {
        return definitions.stream()
                .map(this::convertSupplemental)
                .collect(Collectors.toList());
    }

    private List<Measure.MeasureSupplementalDataComponent> processRiskAdjWrapper(List<RiskAdjustmentDTO> riskAdjs) {
        return riskAdjs.stream()
                .map(this::convertRiskAdj)
                .collect(Collectors.toList());
    }

    private Measure.MeasureSupplementalDataComponent convertSupplemental(CQLDefinition cqlDefinition) {
        Measure.MeasureSupplementalDataComponent supplementalData = new Measure.MeasureSupplementalDataComponent();
        supplementalData.setId(cqlDefinition.getId());
        return supplementalData
                .setUsage(supplementalDataUsage())
                .setCriteria(criteriaExpression(cqlDefinition.getName()));
    }

    private Measure.MeasureSupplementalDataComponent convertRiskAdj(RiskAdjustmentDTO riskAdj) {
        Measure.MeasureSupplementalDataComponent supplementalData = new Measure.MeasureSupplementalDataComponent();
        supplementalData.setId(riskAdj.getUuid());
        return supplementalData
                .setUsage(riskAdjUsage())
                .setCriteria(criteriaExpression(riskAdj.getName()));
    }

    private List<CodeableConcept> supplementalDataUsage() {
        Coding coding = new Coding()
                .setSystem(MEASURE_DATA_USAGE)
                .setCode("supplemental-data");

        List<CodeableConcept> mList = new ArrayList<>(1);
        mList.add(new CodeableConcept().addCoding(coding));
        return mList;
    }

    private List<CodeableConcept> riskAdjUsage() {
        Coding coding = new Coding()
                .setSystem(MEASURE_DATA_USAGE)
                .setCode("risk-adjustment-factor");

        List<CodeableConcept> mList = new ArrayList<>(1);
        mList.add(new CodeableConcept().addCoding(coding));
        return mList;
    }

    private Expression criteriaExpression(String expression) {
        return new Expression()
                .setLanguage("text/cql")
                .setExpression(expression);
    }
}
