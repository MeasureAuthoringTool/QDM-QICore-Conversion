package gov.cms.mat.qdmqicore.mapping.model.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class GoogleMatAttributes {
    @JsonProperty("title")
    Cell title;
    @JsonProperty("updated")
    Cell updated;
    @JsonProperty("gsx$matdatatypedescription")
    Cell matDataTypeDescription;
    @JsonProperty("gsx$matattributename")
    Cell matAttributeName;
    @JsonProperty("gsx$matdatatypeid")
    Cell matDataTypeId;
    @JsonProperty("gsx$matattributeid")
    Cell matAttributeId;
    @JsonProperty("gsx$fhirr4qicoremapping")
    Cell fhirR4QiCoreMapping;
    @JsonProperty("gsx$fhirresource")
    Cell fhirResource;
    @JsonProperty("gsx$fhirelement")
    Cell fhirElement;
    @JsonProperty("gsx$fhirtype")
    Cell fhirType;
    @JsonProperty("gsx$helpwording")
    Cell helpWording;
    @JsonProperty("gsx$dropdownvalues")
    Cell dropDown;
    @JsonProperty("gsx$commentforynmodemodedetails")
    Cell recommendations;
}
