package gov.cms.mat.fhir.services.summary;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.mongo.ConversionKey;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.FhirLibraryNotFoundException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.ValueSet;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@ToString
public class OrchestrationProperties {
    final List<ValueSet> valueSets = new ArrayList<>();
    final List<CqlLibrary> cqlLibraries = new ArrayList<>();

    final List<Library> fhirLibraries = new ArrayList<>();

    ConversionKey threadLocalKey;

    Measure matMeasure;

    @Setter
    org.hl7.fhir.r4.model.Measure fhirMeasure;

    ConversionType conversionType;
    XmlSource xmlSource;

    public String getMeasureId() {
        if (matMeasure != null) {
            return matMeasure.getId();
        } else {
            return null;
        }
    }

    public Library findFhirLibrary(String id) {
        return fhirLibraries.stream()
                .findFirst()
                .filter(l -> l.getId().equals(id))
                .orElseThrow(() -> new FhirLibraryNotFoundException(id));
    }

}
