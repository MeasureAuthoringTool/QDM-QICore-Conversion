package gov.cms.mat.qdmqicore.mapping.model.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class ConversionDataType {
    @JsonProperty("qdmType")
    private String qdmType;
    @JsonProperty("fhirType")
    private String fhirType;
    @JsonProperty("whereAdjustment")
    private String whereAdjustment;
    @JsonProperty("comment")
    private String comment;
}