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
    private Cell qdmType;
    @JsonProperty("gsx$qdmattrib")
    private Cell qdmAttribute;
    @JsonProperty("gsx$fhirtype")
    private Cell fhirType;
    @JsonProperty("gsx$fhirattrib")
    private Cell fhirAttribute;
    @JsonProperty("gsx$comment")
    private Cell comment;
}
