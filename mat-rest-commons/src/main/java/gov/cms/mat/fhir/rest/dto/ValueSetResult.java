package gov.cms.mat.fhir.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValueSetResult {
    String reason;
    Boolean success;
    String link;
}