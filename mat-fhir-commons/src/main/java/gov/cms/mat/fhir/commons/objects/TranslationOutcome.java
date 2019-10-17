/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.commons.objects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author duanedecouteau
 */
@XmlRootElement
public class TranslationOutcome {
    private Boolean successful = Boolean.TRUE;
    private String message = "";
    private String fhirIdentity;

    /**
     * @return the successful
     */
    @XmlElement(name = "successful")
    public Boolean getSuccessful() {
        return successful;
    }

    /**
     * @param successful the successful to set
     */
    public void setSuccessful(Boolean successful) {
        this.successful = successful;
    }

    /**
     * @return the message
     */
    @XmlElement(name = "message")
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the fhirIdentity
     */
    public String getFhirIdentity() {
        return fhirIdentity;
    }

    /**
     * @param fhirIdentity the fhirIdentity to set
     */
    public void setFhirIdentity(String fhirIdentity) {
        this.fhirIdentity = fhirIdentity;
    }
    
}
