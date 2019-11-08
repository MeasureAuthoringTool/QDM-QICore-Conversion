package gov.cms.mat.qdmqicore.conversion.spread_sheet_data;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
@Slf4j
@RefreshScope
public class FhirQdmMappingData {
    private final RestTemplate restTemplate;

    @Getter
    private ConversionData conversionData;

    @Value("${json.data.url}")
    private String url;

    public FhirQdmMappingData(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @EventListener(RefreshScopeRefreshedEvent.class)
    public void onRefresh(RefreshScopeRefreshedEvent event) {
        log.info("Processed event: {} ", event.getName());
    }

    @PostConstruct
    void postConstruct() {
        conversionData = restTemplate.getForObject(url, ConversionData.class);
        log.info("Received {} records from the spreadsheet's JSON", conversionData.feed.getEntry().size());
    }

    public List<ConversionEntry> getAll() {
        return conversionData.getFeed().getEntry();
    }
}
