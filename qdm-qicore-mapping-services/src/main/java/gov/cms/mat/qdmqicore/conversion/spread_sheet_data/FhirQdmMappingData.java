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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    static boolean areAllUnique(List<ConversionEntry> list) {
        Set<Integer> set = new HashSet<>();

        for (ConversionEntry t : list) {
            if (!set.add(t.hashCode())) {
                log.error("Record is a dup: {}", t.hashCode());
                return false;
            }
        }

        return true;
    }

    @EventListener(RefreshScopeRefreshedEvent.class)
    public void onRefresh(RefreshScopeRefreshedEvent event) {
        log.info("Processed event: {} ", event.getName());
    }

    @PostConstruct
    void postConstruct() {
        conversionData = restTemplate.getForObject(url, ConversionData.class);
        log.info("Received {} records from the spreadsheet's JSON, URL: {}",
                conversionData.feed.getEntry().size(), url);

        if (!areAllUnique(conversionData.feed.getEntry())) {
            log.error("We have duplicate data from the spreadsheet");
        } else {
            log.info("All records are uniq from the spreadsheet");
        }
    }

    public List<ConversionEntry> getAll() {
        return conversionData.getFeed().getEntry();
    }
}
