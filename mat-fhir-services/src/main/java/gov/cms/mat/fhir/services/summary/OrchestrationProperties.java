package gov.cms.mat.fhir.services.summary;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.reporting.ThreadSessionKey;
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
@Setter
@Slf4j
@ToString
public class OrchestrationProperties {
    private final List<ValueSet> valueSets = new ArrayList<>();
    private final List<Library> fhirLibraries = new ArrayList<>();
    private CqlLibrary measureLib;
    private ThreadSessionKey threadSessionKey;
    private Measure matMeasure;
    private String vsacApiKey;
    private boolean showWarnings;
    private boolean includeStdLibs;
    private boolean isPush;
    private org.hl7.fhir.r4.model.Measure fhirMeasure;
    private ConversionType conversionType;
    private XmlSource xmlSource;

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
