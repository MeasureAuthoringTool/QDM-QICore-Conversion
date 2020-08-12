package gov.cms.mat.qdmqicore.mapping.model.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleFhirLightBoxDatatypeAttributeAssociationTypes {
    @JsonProperty("gsx$datatype")
    private Cell datatype;

    @JsonProperty("gsx$attribute")
    private Cell attribute;

    @JsonProperty("gsx$attributetype")
    private Cell attributeType;

    @JsonProperty("gsx$hasbinding")
    private Cell hasBinding;
}

