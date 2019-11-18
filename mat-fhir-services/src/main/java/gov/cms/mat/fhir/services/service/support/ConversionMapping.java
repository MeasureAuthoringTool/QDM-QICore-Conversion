package gov.cms.mat.fhir.services.service.support;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ConversionMapping {
    String title;

    String matDataTypeDescription;

    String matAttributeName;

    String fhirR4QiCoreMapping;

    String fhirResource;

    String fhirElement;

    String fhirType;
}
