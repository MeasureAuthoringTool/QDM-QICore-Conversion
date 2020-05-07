package gov.cms.mat.fhir.services.service;

import gov.cms.mat.cql.CqlNegations;
import gov.cms.mat.fhir.rest.dto.spreadsheet.QdmToQicoreMapping;
import gov.cms.mat.fhir.rest.dto.spreadsheet.QdmToFhirMappingHelper;
import gov.cms.mat.fhir.services.exceptions.QdmQiCoreDataException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class QdmQiCoreDataService {
    private String[] negations = CqlNegations.getNegations();
    private final RestTemplate restTemplate;

    @Value("${qdmqicore.conversion.baseurl}/qdmToQicoreMappings")
    private String matAtttributesUrl;

    @Resource
    private QdmQiCoreDataService self;


    public QdmQiCoreDataService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Example: KEY: Allergy/Intolerance Value: AllergyIntolerance
     * @return a map of qdm type to fhir types.
     */
    @Cacheable("qdmToQiCoreMappingHelper")
    public QdmToFhirMappingHelper getQdmToFhirMappingHelper() {
        try {
            Map<String,String> result = new HashMap<>();
            QdmToQicoreMapping[] mappings = restTemplate.getForObject(matAtttributesUrl, QdmToQicoreMapping[].class);
            if (mappings == null) {
                throw new QdmQiCoreDataException("No results found");
            }
            return new QdmToFhirMappingHelper(Arrays.asList(mappings));
        } catch (RestClientException e) {
            log.warn("Cannot get FhirR4QiCoreMapping: {}", "All", e);
            throw new QdmQiCoreDataException(e.getMessage());
        }
    }
}
