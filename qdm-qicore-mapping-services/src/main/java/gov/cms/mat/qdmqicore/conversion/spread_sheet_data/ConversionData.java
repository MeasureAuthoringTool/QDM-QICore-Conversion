package gov.cms.mat.qdmqicore.conversion.spread_sheet_data;

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
        List<ConversionEntry> entry;
    }
}
