package gov.cms.mat.fhir.services.translate.processor;

import gov.cms.mat.fhir.services.components.mat.MatXmlConverter;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.translate.creators.FhirCreator;
import mat.model.cql.CQLDefinition;
import mat.model.cql.CQLDefinitionsWrapper;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Expression;
import org.hl7.fhir.r4.model.Measure;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SupplementalDataProcessor implements FhirCreator {
    public static final String MEASURE_DATA_USAGE = "http://hl7.org/fhir/measure-data-usage";

    private final MatXmlConverter matXmlConverter;

    public SupplementalDataProcessor(MatXmlConverter matXmlConverter) {
        this.matXmlConverter = matXmlConverter;
    }

    public List<Measure.MeasureSupplementalDataComponent> processXml(String xml) {
        CQLDefinitionsWrapper cqlDefinitionsWrapper = matXmlConverter.toCQLDefinitionsSupplementalData(xml);

        if (CollectionUtils.isEmpty(cqlDefinitionsWrapper.getCqlDefinitions())) {
            ConversionReporter.setMeasureResult("MAT.supplementalData",
                    "Measure.supplementalData",
                    "No SupplementalData");
            return Collections.emptyList();
        } else {
            return processWrapper(cqlDefinitionsWrapper.getCqlDefinitions());
        }
    }

    private List<Measure.MeasureSupplementalDataComponent> processWrapper(List<CQLDefinition> definitions) {
        return definitions.stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    private Measure.MeasureSupplementalDataComponent convert(CQLDefinition cqlDefinition) {
        Measure.MeasureSupplementalDataComponent supplementalData = new Measure.MeasureSupplementalDataComponent();

        return supplementalData
                .setCode(new CodeableConcept().setText(makeCodeFromName(cqlDefinition.getName())))
                .setUsage(processSupplementalDataUsage())
                .setCriteria(processSupplementalDataCriteria(cqlDefinition.getName()));
    }

    private String makeCodeFromName(String name) {
        if (StringUtils.isEmpty(name)) {
            return "unknown";
        } else {
            // Convert "SDE Race" to "sde-race"
            String code = name.toLowerCase().trim();
            return code.replace(' ', '-');
        }
    }

    private List<CodeableConcept> processSupplementalDataUsage() {
        Coding coding = new Coding()
                .setSystem(MEASURE_DATA_USAGE)
                .setCode("supplemental-data");

        List<CodeableConcept> mList = new ArrayList<>(1);
        mList.add(new CodeableConcept().addCoding(coding));
        return mList;
    }

    private Expression processSupplementalDataCriteria(String expression) {
        return new Expression()
                .setLanguage("text/cql")
                .setExpression(expression);
    }
}
