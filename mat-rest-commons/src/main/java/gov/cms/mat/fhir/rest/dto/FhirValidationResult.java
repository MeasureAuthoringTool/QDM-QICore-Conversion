package gov.cms.mat.fhir.rest.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FhirValidationResult {
    String severity;
    String locationField;
    String errorDescription;
}
