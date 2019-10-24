package gov.cms.mat.fhir.services.service;

import mat.model.cql.CQLQualityDataSetDTO;

public interface VsacService {
    boolean validateUser();

    boolean validateTicket();

    boolean getData(CQLQualityDataSetDTO cqlQualityDataSetDTO);
}
