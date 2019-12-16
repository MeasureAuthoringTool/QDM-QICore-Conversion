package gov.cms.mat.fhir.commons.objects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class FhirResourceValidationResult {
    private String type;
    private String id;
    private List<FhirResourceValidationError> validationErrorList = new ArrayList<>();
    private String measureId;

    public FhirResourceValidationResult() {
    }

    public FhirResourceValidationResult(String id, String type) {
        this.id = id;
        this.type = type;
    }

    /**
     * @return the type
     *
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
    public List<FhirResourceValidationError> getValidationErrorList() {
        return validationErrorList;
    }

    @XmlElement
    public String getMeasureId() {
        return measureId;
    }

    public void setMeasureId(String measureId) {
        this.measureId = measureId;
    }
}
