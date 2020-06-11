package gov.cms.mat.fhir.services.summary;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.mongo.ThreadSessionKey;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.exceptions.FhirLibraryNotFoundException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.ValueSet;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Slf4j
@ToString
public class OrchestrationProperties {
    final List<ValueSet> valueSets = new ArrayList<>();
    @Setter
    CqlLibrary measureLib;

    final List<Library> fhirLibraries = new ArrayList<>();

    ThreadSessionKey threadSessionKey;

    @Setter
    Measure matMeasure;

    String vsacGrantingTicket;

    boolean showWarnings;

    Boolean includeStdLibs ;

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
                .filter(l -> l.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new FhirLibraryNotFoundException(id));
    }
}
