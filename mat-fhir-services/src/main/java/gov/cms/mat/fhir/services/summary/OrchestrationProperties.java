package gov.cms.mat.fhir.services.summary;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.hl7.fhir.r4.model.ValueSet;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@ToString
public class OrchestrationProperties {
    final List<ValueSet> valueSets = new ArrayList<>();
    final List<CqlLibrary> cqlLibraries = new ArrayList<>();

    Measure matMeasure;
    ConversionType conversionType;
    XmlSource xmlSource;

    public String getMeasureId() {
        if (matMeasure != null) {
            return matMeasure.getId();
        } else {
            return null;
        }
    }
}
