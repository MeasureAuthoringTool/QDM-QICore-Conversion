package gov.cms.mat.qdmqicore.mapping.model.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class QdmToQicoreMapping {
    @JsonProperty("title")
    private Cell title;

    // input doesn't contain matDataType
    @JsonProperty("gsx$matdatatype")
    private Cell matDataType;

    @JsonProperty("matAttributeNameOriginal")
    private Cell matAttributeNameOriginal;

    @JsonProperty("matAttributeNameNormal")
    private Cell matAttributeNameNormal;

    @JsonProperty("fhirR4QicoreMapping")
    private Cell fhir4QiCoreMapping;

    @JsonProperty("comment")
    private Cell comment;

    @JsonProperty("qdmContextVersusAttributes")
    private Cell qdmContextVersusAttributes;

    @JsonProperty("publicationOfMapDate")
    private Cell publicationOfMapDate;

    @JsonProperty("elementId")
    private Cell elementId;

    @JsonProperty("definition")
    private Cell definition;

    @JsonProperty("cardinality")
    private Cell cardinality;

    @JsonProperty("terminologyBinding")
    private Cell terminologyBinding;

    @JsonProperty("type")
    private Cell type;

    @JsonProperty("isModifier")
    private Cell isModifier;

    @JsonProperty("altNames")
    private Cell altNames;

    @JsonProperty("summary")
    private Cell summary;

    @JsonProperty("compositeHelp")
    private Cell compositeHelp;

    @JsonProperty("sourcePage")
    private Cell sourcePage;
}
