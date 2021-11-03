package gov.cms.mat.qdmqicore.mapping.model.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataTypes {
    @JsonProperty("dataType")
    private String dataType;

    @JsonProperty("validValues")
    private String validValues;

    @JsonProperty("regex")
    private String regex;

    @JsonProperty("type")
    private String type;
}
