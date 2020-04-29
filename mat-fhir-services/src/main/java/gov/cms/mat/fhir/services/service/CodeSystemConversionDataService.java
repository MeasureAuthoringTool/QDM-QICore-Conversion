package gov.cms.mat.fhir.services.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import gov.cms.mat.fhir.services.summary.CodeSystemEntry;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CodeSystemConversionDataService {
    private final RestTemplate restTemplate;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GoogleConversionData {
        Feed feed;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Feed {
        List<GoogleCodeSystemEntry> entry;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Slf4j
    @EqualsAndHashCode
    public static class GoogleCodeSystemEntry {
        @JsonProperty("gsx$oid")
        Cell oid;

        @JsonProperty("gsx$url")
        Cell url;

        @JsonProperty("gsx$codesystemname")
        Cell name;

        @JsonProperty("gsx$defaultvsacversion")
        Cell defaultVsacVersion;


        public String getOidData() {
            return getCellData(oid);
        }

        public String getUrlData() {
            return getCellData(url);
        }

        public String getNameData() {
            return getCellData(name);
        }

        public String getDefaultVsacVersionData() {
            return getCellData(defaultVsacVersion);
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

    @Value("${json.data.url}")
    private String url;

    @Getter
    private List<CodeSystemEntry> codeSystemMappings;

    public CodeSystemConversionDataService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<CodeSystemEntry> reload() {
        List<CodeSystemEntry> result = new ArrayList<>();
        GoogleConversionData conversionData = restTemplate.getForObject(url, GoogleConversionData.class);
        log.info("Received {} records from the spreadsheet's JSON, URL: {}",
                conversionData.getFeed().getEntry().size(), url);

        conversionData.getFeed().getEntry().forEach(gcs ->
                result.add(new CodeSystemEntry(gcs.getOidData(),gcs.getUrlData(),gcs.getNameData(),gcs.getDefaultVsacVersionData())));
        codeSystemMappings = result;
        return result;
    }

    @PostConstruct
    public void postConstruct() {
        reload();
    }
}
