package gov.cms.mat.fhir.commons.objects;

import lombok.ToString;





@ToString
public class FhirResourceValidationError {
    private String severity;
    private String locationField;
    private String errorDescription;
    
    public FhirResourceValidationError(String severity, String locationField, String errorDescription) {
        this.severity = severity;
        this.locationField = locationField;
        this.errorDescription = errorDescription;
    }

    /**
     * @return the severity
     */

    public String getSeverity() {
        return severity;
    }

    /**
     * @param severity the severity to set
     */
    public void setSeverity(String severity) {
        this.severity = severity;
    }

    /**
     * @return the locationField
     */

    public String getLocationField() {
        return locationField;
    }

    /**
     * @param locationField the locationField to set
     */
    public void setLocationField(String locationField) {
        this.locationField = locationField;
    }

    /**
     * @return the errorDescription
     */

    public String getErrorDescription() {
        return errorDescription;
    }

    /**
     * @param errorDescription the errorDescription to set
     */
    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }
    
}
