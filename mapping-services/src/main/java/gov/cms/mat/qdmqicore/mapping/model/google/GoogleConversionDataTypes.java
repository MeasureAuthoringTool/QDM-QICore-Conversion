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
    private Cell qdmType;
    @JsonProperty("gsx$fhirtype")
    private Cell fhirType;
    @JsonProperty("gsx$whereadjustment")
    private Cell whereAdjustment;
    @JsonProperty("gsx$comment")
    private Cell comment;
}
