package gov.cms.mat.fhir.commons.objects;



import java.util.ArrayList;
import java.util.List;


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

    public List<FhirResourceValidationError> getValidationErrorList() {
        return validationErrorList;
    }


    public String getMeasureId() {
        return measureId;
    }

    public void setMeasureId(String measureId) {
        this.measureId = measureId;
    }
}
