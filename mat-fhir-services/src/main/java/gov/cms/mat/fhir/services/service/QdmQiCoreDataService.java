package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.rest.dto.ConversionMapping;
import gov.cms.mat.fhir.services.exceptions.QdmQiCoreDataException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class QdmQiCoreDataService {
    private final RestTemplate restTemplate;
    private String[] exludeList = new String[]{"Not Performed", "Not Ordered", "Not Recommended", "Not Administered", "Not Dispensed"};

    @Value("${qdmqicore.conversion.baseurl}")
    private String baseURL;

    public QdmQiCoreDataService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public ConversionMapping findByFhirR4QiCoreMapping(String matObjectWithAttribute, String fhirR4QiCoreMapping) {
        URI uri = buildUri(matObjectWithAttribute, fhirR4QiCoreMapping);
        log.debug("Finding spread sheet data: {}", uri);

        try {
            return restTemplate.getForObject(uri, ConversionMapping.class);
        } catch (RestClientException e) {
            log.warn("Cannot get FhirR4QiCoreMapping: {}", fhirR4QiCoreMapping, e);
            throw new QdmQiCoreDataException(e.getMessage());
        }
    }

    public List<ConversionMapping> findAllFilteredByMatDataTypeDescription(String matDataTypeDescription) {
        URI uri = buildUriForAllFilteredByMatDataTypeDescription(matDataTypeDescription);
        log.info("Finding All spreadsheet data by matDataTypeDescription: {}", uri);
        return restExcchange(uri);
    }

    public List<ConversionMapping> findAllFiltered() {
        URI uri = buildUriForAllFiltered();
        log.info("Finding All spreadsheet data: {}", uri);
        return restExcchange(uri);
    }

    public List<ConversionMapping> restExcchange(URI uri) {
        try {
            ConversionMapping[] mappings = restTemplate.getForObject(uri, ConversionMapping[].class);

            if (mappings == null) {
                throw new QdmQiCoreDataException("No results found");
            } else {
                return Arrays.asList(mappings);
            }
        } catch (RestClientException e) {
            log.warn("Cannot get FhirR4QiCoreMapping: {}", "All", e);
            throw new QdmQiCoreDataException(e.getMessage());
        }
    }

    private URI buildUriForAllFiltered() {
        return UriComponentsBuilder
                .fromHttpUrl(baseURL + "/filtered")
                .build()
                .encode()
                .toUri();
    }

    private URI buildUriForAllFilteredByMatDataTypeDescription(String matDataTypeDescription) {
        return UriComponentsBuilder
                .fromHttpUrl(baseURL + "/filtered")
                .queryParam("matDataTypeDescription", matDataTypeDescription)
                .build()
                .encode()
                .toUri();
    }

    private URI buildUri(String matObjectWithAttribute, String fhirR4QiCoreMapping) {
        QdmQiCoreDataService.MatObject matObject = parseMatObject(matObjectWithAttribute);

        return UriComponentsBuilder
                .fromHttpUrl(baseURL + "/findOneByFhirR4QiCoreMapping")
                .queryParam("fhirR4QiCoreMapping", fhirR4QiCoreMapping)
                .queryParam("matAttributeName", matObject.attribute)
                .queryParam("matDataTypeDescription", matObject.name)
                .build()
                .encode()
                .toUri();
    }

    private MatObject parseMatObject(String matObjectWithAttribute) {
        if (StringUtils.isEmpty(matObjectWithAttribute)) {
            throw new QdmQiCoreDataException("matObjectWithAttribute is null or empty");
        }

        String[] splits = matObjectWithAttribute.split("\\.");

        if (splits.length != 2) {
            throw new QdmQiCoreDataException("Cannot parse matObjectWithAttribute: " + matObjectWithAttribute);
        }

        return new MatObject(splits[0], splits[1]);
    }

    @AllArgsConstructor
    private static class MatObject {
        final String name;
        final String attribute;
    }

}
