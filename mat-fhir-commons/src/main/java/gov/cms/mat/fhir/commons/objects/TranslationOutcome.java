package gov.cms.mat.fhir.commons.objects;


public class TranslationOutcome {
    private Boolean successful = Boolean.TRUE;
    private String message = "";
    private String fhirIdentity;

    /**
     * @return the successful
     */
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
