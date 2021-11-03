package gov.cms.mat.qdmqicore.mapping.model.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class ConversionAttribute {
    @JsonProperty("qdmType")
    private String qdmType;
    @JsonProperty("qdmAttribute")
    private String qdmAttribute;
    @JsonProperty("fhirType")
    private String fhirType;
    @JsonProperty("fhirAttribute")
    private String fhirAttribute;
    @JsonProperty("comment")
    private String comment;
}
