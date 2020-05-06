package gov.cms.mat.fhir.rest.dto.spreadsheet;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResourceDefinition {
    private String elementId;
    private String definition;
    private String cardinality;
    private String type;
    private String isSummary;
    private String isModifier;
}
