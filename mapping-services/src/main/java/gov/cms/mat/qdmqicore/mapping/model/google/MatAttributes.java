package gov.cms.mat.qdmqicore.mapping.model.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class MatAttributes {
    @JsonProperty("dataTypeDescription")
    String dataTypeDescription;
    @JsonProperty("matAttributeName")
    String matAttributeName;
    @JsonProperty("fhirQicoreMapping")
    String fhirQicoreMapping;
    @JsonProperty("fhirResource")
    String fhirResource;
    @JsonProperty("fhirType")
    String fhirType;
    @JsonProperty("fhirElement")
    String fhirElement;
    @JsonProperty("helpWording")
    String helpWording;
    @JsonProperty("dropDown")
    List<String> dropDown;
}
