package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.services.service.support.ConversionMapping;

public interface QdmQiCoreDataService {
    ConversionMapping findByFhirR4QiCoreMapping(String matObjectWithAttribute, String fhirR4QiCoreMapping);

}
