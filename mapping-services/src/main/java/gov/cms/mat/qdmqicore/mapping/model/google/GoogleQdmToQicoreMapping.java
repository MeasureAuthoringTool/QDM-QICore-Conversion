package gov.cms.mat.qdmqicore.mapping.model.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class GoogleQdmToQicoreMapping {
    @JsonProperty("title")
    private Cell title;

    @JsonProperty("gsx$matdatatype")
    private Cell matDataType;

    @JsonProperty("gsx$matattributenameoriginal")
    private Cell matAttributeNameOriginal;

    @JsonProperty("gsx$matattributenamenormal")
    private Cell matAttributeNameNormal;

    @JsonProperty("gsx$fhirr4qicoremapping")
    private Cell fhir4QiCoreMapping;

    @JsonProperty("gsx$comment")
    private Cell comment;

    @JsonProperty("gsx$qdmcontextversusattributes")
    private Cell qdmContextVersionAttributes;

    @JsonProperty("gsx$publicationofmapdate")
    private Cell publicationOfMapDate;

    @JsonProperty("gsx$elementid")
    private Cell elementId;

    @JsonProperty("gsx$definition")
    private Cell definition;

    @JsonProperty("gsx$cardinality")
    private Cell cardinality;

    @JsonProperty("gsx$terminologybinding")
    private Cell terminologyBinding;

    @JsonProperty("gsx$type")
    private Cell type;

    @JsonProperty("gsx$ismodifier")
    private Cell isModifier;

    @JsonProperty("gsx$altnames")
    private Cell altNames;

    @JsonProperty("gsx$summary")
    private Cell summary;

    @JsonProperty("gsx$compositehelp")
    private Cell compositeHelp;

    @JsonProperty("gsx$sourcepage")
    private Cell sourcePage;
}
