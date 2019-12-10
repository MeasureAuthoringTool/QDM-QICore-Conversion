/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.commons.objects;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author duanedecouteau
 */
@XmlRootElement
public class FhirResourceValidationResult {
    private String type;
    private String id;
    private List<FhirResourceValidationError> errorList = new ArrayList<>();

    /**
     * @return the type
     */
    @XmlElement
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the id
     */
    @XmlElement
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the errorList
     */
    @XmlElement
    public List<FhirResourceValidationError> getErrorList() {
        return errorList;
    }

    /**
     * @param errorList the errorList to set
     */
    public void setErrorList(List<FhirResourceValidationError> errorList) {
        this.errorList = errorList;
    }
    
    
}
