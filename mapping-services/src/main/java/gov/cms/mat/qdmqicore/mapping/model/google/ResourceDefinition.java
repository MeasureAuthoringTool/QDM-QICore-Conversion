package gov.cms.mat.qdmqicore.mapping.model.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceDefinition {
    @JsonProperty("elementId")
    private Cell elementId;

    @JsonProperty("definition")
    private Cell definition;

    @JsonProperty("cardinality")
    private Cell cardinality;

    @JsonProperty("type")
    private Cell type;

    @JsonProperty("summary")
    private Cell summary;

    @JsonProperty("isModifier")
    private Cell isModifier;
}
