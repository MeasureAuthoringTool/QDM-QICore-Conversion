package gov.cms.mat.qdmqicore.mapping.model.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class GoogleConversionDataTypes {
    @JsonProperty("gsx$qdmtype")
    Cell qdmType;
    @JsonProperty("gsx$fhirtype")
    Cell fhirType;
    @JsonProperty("gsx$whereadjustment")
    Cell whereAdjustment;
    @JsonProperty("gsx$comment")
    Cell comment;
}
