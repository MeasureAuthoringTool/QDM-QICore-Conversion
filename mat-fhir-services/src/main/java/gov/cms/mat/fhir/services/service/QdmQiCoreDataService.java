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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@Slf4j
public class QdmQiCoreDataService {
    private final RestTemplate restTemplate;
    private String[] exludeList = new String[]{"Not Performed", "Not Ordered", "Not Recommended", "Not Administered", "Not Dispensed"};

    @Value("${qdmqicore.conversion.baseurl}")
    private String baseURL = "http://localhost:9090";

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
    
    public List<ConversionMapping> findAll() {
        URI uri = buildUriForAll();
        List<ConversionMapping> resultList =  new ArrayList<ConversionMapping>();
        log.debug("Finding All spreadsheet data: {}", uri);
        try {
           
           ConversionMapping[] tRes = restTemplate.getForObject(uri, ConversionMapping[].class);

           // for timebeing just get rid of the negation values

           for (int i = 0; i < tRes.length; i++) {
               ConversionMapping cm = tRes[i];
               String matDataType = cm.getMatDataTypeDescription();
               if (StringUtils.indexOfAny(matDataType, exludeList) < 0) {
                   if (cm.getFhirResource() == null || cm.getFhirResource().isEmpty() || cm.getFhirElement() == null || cm.getFhirElement().isEmpty()) {
                       // do nothing
                   }
                   else {
                        resultList.add(cm);
                   }
               }
           }
        } catch (RestClientException e) {
            log.warn("Cannot get FhirR4QiCoreMapping: {}", "All", e);
            throw new QdmQiCoreDataException(e.getMessage());            
        }
        return resultList;
    }
    
    private URI buildUriForAll() {
        return UriComponentsBuilder
                .fromHttpUrl(baseURL + "/all")
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
