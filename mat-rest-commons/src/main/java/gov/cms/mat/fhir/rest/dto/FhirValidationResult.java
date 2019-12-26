package gov.cms.mat.fhir.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FhirValidationResult {
    String severity;
    String locationField;
    String errorDescription;
}
