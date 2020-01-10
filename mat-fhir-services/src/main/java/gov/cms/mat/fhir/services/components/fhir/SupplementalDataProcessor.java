package gov.cms.mat.fhir.services.components.fhir;

import gov.cms.mat.fhir.services.components.mat.MatXmlConverter;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.translate.creators.FhirCreator;
import mat.model.cql.CQLDefinition;
import mat.model.cql.CQLDefinitionsWrapper;
import org.hl7.fhir.r4.model.Measure;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SupplementalDataProcessor implements FhirCreator {
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
        Measure.MeasureSupplementalDataComponent supplementalDataComponent = new Measure.MeasureSupplementalDataComponent();

        supplementalDataComponent.setCode(buildCodeableConcept("supplemental-data",
                "http://hl7.org/fhir/measure-data-usage",
                ""));
        supplementalDataComponent.setDescription(cqlDefinition.getName());

        return supplementalDataComponent;
    }
}
