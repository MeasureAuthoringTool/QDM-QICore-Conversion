package gov.cms.mat.fhir.services.summary;

import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OrchestrationProperties {
    Measure matMeasure;
    ConversionType conversionType;
    XmlSource xmlSource;
}
