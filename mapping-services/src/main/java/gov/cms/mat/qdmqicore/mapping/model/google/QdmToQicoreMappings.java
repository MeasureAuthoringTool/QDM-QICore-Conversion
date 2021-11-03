package gov.cms.mat.qdmqicore.mapping.model.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class QdmToQicoreMappings {
    @JsonProperty("title")
    private String title;
    @JsonProperty("matAttributeType")
    private String matAttributeType;
    @JsonProperty("fhirQICoreMapping")
    private String fhirQICoreMapping;
    @JsonProperty("type")
    private String type;
    @JsonProperty("cardinality")
    private String cardinality;
}
