package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.services.exceptions.QdmQiCoreDataException;
import gov.cms.mat.fhir.services.service.support.ConversionMapping;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
@Slf4j
public class QdmQiCoreDataServiceImpl implements QdmQiCoreDataService {
    private final RestTemplate restTemplate;

    @Value("${qdmqicore.conversion.baseurl}")
    private String baseURL;

    public QdmQiCoreDataServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
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

    private URI buildUri(String matObjectWithAttribute, String fhirR4QiCoreMapping) {
        MatObject matObject = parseMatObject(matObjectWithAttribute);

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
