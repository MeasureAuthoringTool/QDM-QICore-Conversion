package gov.cms.mat.qdmqicore.mapping.model.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleResourceDefinition {
    @JsonProperty("gsx$elementid")
    private Cell elementId;

    @JsonProperty("gsx$definition")
    private Cell definition;

    @JsonProperty("gsx$cardinality")
    private Cell cardinality;

    @JsonProperty("gsx$type")
    private Cell type;

    @JsonProperty("gsx$summary")
    private Cell isSummary;

    @JsonProperty("gsx$ismodifier")
    private Cell isModifier;
}
