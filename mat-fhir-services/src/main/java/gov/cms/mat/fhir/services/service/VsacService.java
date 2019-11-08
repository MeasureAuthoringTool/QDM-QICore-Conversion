package gov.cms.mat.fhir.services.service;

import mat.model.VSACValueSetWrapper;

public interface VsacService {
    boolean validateUser();

    boolean validateTicket();

    VSACValueSetWrapper getData(String oid);
}
