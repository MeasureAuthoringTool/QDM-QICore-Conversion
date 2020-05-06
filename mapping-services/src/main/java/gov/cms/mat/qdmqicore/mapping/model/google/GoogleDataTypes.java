package gov.cms.mat.qdmqicore.mapping.model.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleDataTypes {
    @JsonProperty("gsx$datatype")
    private Cell dataType;

    @JsonProperty("gsx$validvalues")
    private Cell validValues;

    @JsonProperty("gsx$regex")
    private Cell regex;

    @JsonProperty("gsx$fieldtype")
    private Cell type;
}
