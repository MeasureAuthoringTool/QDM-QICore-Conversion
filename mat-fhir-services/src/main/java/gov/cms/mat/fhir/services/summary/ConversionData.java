package gov.cms.mat.fhir.services.summary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConversionData {
    Feed feed;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Feed {
        List<CodeSystemEntry> entry;
    }
}

