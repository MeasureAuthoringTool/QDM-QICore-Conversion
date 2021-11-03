package gov.cms.mat.qdmqicore.mapping.model.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceDefinitions {
    @JsonProperty("elementId")
    private String elementId;

    @JsonProperty("definition")
    private String definition;

    @JsonProperty("cardinality")
    private String cardinality;

    @JsonProperty("type")
    private String type;

    @JsonProperty("isSummary")
    private String isSummary;

    @JsonProperty("isModifier")
    private String isModifier;
}
