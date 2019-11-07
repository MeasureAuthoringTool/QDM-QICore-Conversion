package gov.cms.mat.qdmqicore.conversion.spread_sheet_data;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
@Slf4j
public class FhirQdmMappingData {
    private final RestTemplate restTemplate;

    @Getter
    private ConversionData conversionData;

    @Value("${json.data.url}")
    private String url;

    public FhirQdmMappingData(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    void postConstruct() {
        conversionData = restTemplate.getForObject(url, ConversionData.class);
        log.debug("c: {}", conversionData);
    }

    public List<ConversionEntry> getAll() {
        return conversionData.getFeed().getEntry();
    }
}
