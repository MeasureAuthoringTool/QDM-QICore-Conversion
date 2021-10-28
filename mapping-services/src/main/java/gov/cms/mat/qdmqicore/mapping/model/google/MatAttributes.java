package gov.cms.mat.qdmqicore.mapping.model.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class MatAttributes {
    @JsonProperty("title")
    Cell title;
    @JsonProperty("updated")
    Cell updated;
    @JsonProperty("matDataTypeDescription")
    Cell matDataTypeDescription;
    @JsonProperty("matAttributeName")
    Cell matAttributeName;
    @JsonProperty("matDataTypeId")
    Cell matDataTypeId;
    @JsonProperty("matAttributeId")
    Cell matAttributeId;
    @JsonProperty("fhirR4QiCoreMapping")
    Cell fhirR4QiCoreMapping;
    @JsonProperty("fhirResource")
    Cell fhirResource;
    @JsonProperty("fhirElement")
    Cell fhirElement;
    @JsonProperty("fhirType")
    Cell fhirType;
    @JsonProperty("helpWording")
    Cell helpWording;
    @JsonProperty("dropDownValues")
    Cell dropDown;
    @JsonProperty("commentForynModeModeDetails")
    Cell recommendations;
}
