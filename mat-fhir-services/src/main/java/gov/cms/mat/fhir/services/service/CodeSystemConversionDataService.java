package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.services.summary.ConversionData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class CodeSystemConversionDataService {
    private final RestTemplate restTemplate;

    @Value("${json.data.url}")
    private String url;

    @Getter
    private ConversionData conversionData;

    public CodeSystemConversionDataService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void postConstruct() {
        conversionData = restTemplate.getForObject(url, ConversionData.class);
        log.info("Received {} records from the spreadsheet's JSON, URL: {}",
                conversionData.getFeed().getEntry().size(), url);
    }
}
