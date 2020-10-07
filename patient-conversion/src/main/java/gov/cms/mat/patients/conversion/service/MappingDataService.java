package gov.cms.mat.patients.conversion.service;

import gov.cms.mat.fhir.rest.dto.spreadsheet.CodeSystemEntry;
import gov.cms.mat.patients.conversion.exceptions.MappingServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class MappingDataService {
    private final RestTemplate restTemplate;

    @Value("${mapping.services.baseurl}/codeSystemEntries")
    private String codeSystemEntriesUrl;

    public MappingDataService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Cacheable("codeSystemEntries")
    public List<CodeSystemEntry> getCodeSystemEntries() {
        try {
            CodeSystemEntry[] codeSystemEntriesArray = restTemplate.getForObject(codeSystemEntriesUrl, CodeSystemEntry[].class);

            if (codeSystemEntriesArray == null) {
                throw new MappingServiceException("No CodeSystemEntry=ies found, url: " + codeSystemEntriesUrl);
            }

            return Arrays.asList(codeSystemEntriesArray);
        } catch (RestClientException e) {
            log.warn("Cannot get F QdmToQicoreMapping", e);
            throw new MappingServiceException(e.getMessage());
        }
    }

}
