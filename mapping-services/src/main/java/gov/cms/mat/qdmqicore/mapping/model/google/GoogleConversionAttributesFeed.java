package gov.cms.mat.qdmqicore.mapping.model.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleConversionAttributesFeed {
    private List<GoogleConversionAttributes> entry;
}