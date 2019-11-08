package gov.cms.mat.fhir.services.components.mat;

public class MatXmlException extends RuntimeException {
    public MatXmlException(Exception e) {
        super(e);
    }

    public MatXmlException(String message) {
        super(message);
    }
}
