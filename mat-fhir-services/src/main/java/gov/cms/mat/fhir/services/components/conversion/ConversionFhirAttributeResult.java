package gov.cms.mat.fhir.services.components.conversion;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ConversionFhirAttributeResult {
    String type;
    String comment;
    String fhirAttribute;
}
