package gov.cms.mat.patients.conversion.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FhirDataElement {

    String codeListId;
    String valueSetTitle;
    String description;

    @JsonProperty("fhir_resource")
    JsonNode fhirResource;

    ConversionOutcome outcome;
}
