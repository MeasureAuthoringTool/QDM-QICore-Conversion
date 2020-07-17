package gov.cms.mat.qdmqicore.mapping.model.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class GoogleConversionAttributes {
    @JsonProperty("gsx$qdmtype")
    Cell qdmType;
    @JsonProperty("gsx$qdmattrib")
    Cell qdmAttribute;
    @JsonProperty("gsx$fhirtype")
    Cell fhirType;
    @JsonProperty("gsx$fhirattrib")
    Cell fhirAttribute;
}
