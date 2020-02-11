/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.services.cql;

import gov.cms.mat.fhir.services.translate.*;

/**
 *
 * @author duanedecouteau
 */
public class CqlDomainSymbolic {
    private String qdmDomain;
    private String fhirDomain;
    private String symbolic;

    public CqlDomainSymbolic(String qdmDomain, String fhirDomain, String symbolic) {
        this.qdmDomain = qdmDomain;
        this.fhirDomain = fhirDomain;
        this.symbolic = symbolic;
    }
    /**
     * @return the qdmDomain
     */
    public String getQdmDomain() {
        return qdmDomain;
    }

    /**
     * @param qdmDomain the qdmDomain to set
     */
    public void setQdmDomain(String qdmDomain) {
        this.qdmDomain = qdmDomain;
    }

    /**
     * @return the fhirDomain
     */
    public String getFhirDomain() {
        return fhirDomain;
    }

    /**
     * @param fhirDomain the fhirDomain to set
     */
    public void setFhirDomain(String fhirDomain) {
        this.fhirDomain = fhirDomain;
    }

    /**
     * @return the symbolic
     */
    public String getSymbolic() {
        return symbolic;
    }

    /**
     * @param symbolic the symbolic to set
     */
    public void setSymbolic(String symbolic) {
        this.symbolic = symbolic;
    }
    
}
