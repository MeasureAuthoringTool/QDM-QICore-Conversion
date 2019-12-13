package gov.cms.mat.fhir.commons.objects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

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

}
