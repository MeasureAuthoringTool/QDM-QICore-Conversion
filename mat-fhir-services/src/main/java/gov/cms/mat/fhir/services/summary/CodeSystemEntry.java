package gov.cms.mat.fhir.services.summary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
@EqualsAndHashCode
public class CodeSystemEntry {
    @JsonProperty("gsx$oid")
    Cell oid;

    @JsonProperty("gsx$url")
    Cell url;

    @JsonProperty("gsx$codesystemname")
    Cell name;

    public String getOidData() {
        return getCellData(oid);
    }

    public String getUrlData() {
        return getCellData(url);
    }

    public String getNameData() {
        return getCellData(url);
    }

    private String getCellData(Cell cell) {
        return cell == null ? null : cell.getData();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Cell {
        String type;
        @JsonProperty("$t")
        String data;
    }
}
