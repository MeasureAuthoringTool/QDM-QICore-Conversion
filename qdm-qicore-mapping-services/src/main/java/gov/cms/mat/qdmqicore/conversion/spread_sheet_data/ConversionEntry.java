package gov.cms.mat.qdmqicore.conversion.spread_sheet_data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
@EqualsAndHashCode
public class ConversionEntry {
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

    @JsonProperty("gsx$dropdownvalues")
    Cell dropDown;

    @JsonProperty("gsx$recommendations")
    Cell recommendations;

    public String getTitleData() {
        return getCellData(title);
    }

    public String getMatDataTypeDescriptionData() {
        return getCellData(matDataTypeDescription);
    }

    public String getMatAttributeNameData() {
        return getCellData(matAttributeName);
    }

    public String getFhirR4QiCoreMappingData() {
        return getCellData(fhirR4QiCoreMapping);
    }

    public String getFhirResourceData() {
        return getCellData(fhirResource);
    }

    public String getFhirElementData() {
        return getCellData(fhirElement);
    }


    public String getFhirTypeData() {
        return getCellData(fhirType);
    }

    private String getCellData(Cell cell) {
        return cell == null ? null : cell.getData();
    }

    public List<String> getRecommendationsValues() {
        return getStrings(recommendations);
    }

    public List<String> getDropDownValues() {
        return getStrings(dropDown);
    }

    public List<String> getStrings(Cell cell) {
        String cellData = getCellData(cell);

        if (StringUtils.isEmpty(cellData)) {
            return Collections.emptyList();
        } else {
            return Arrays.stream(cellData.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());

        }
    }


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Cell {
        String type;
        @JsonProperty("$t")
        String data;
    }
}

