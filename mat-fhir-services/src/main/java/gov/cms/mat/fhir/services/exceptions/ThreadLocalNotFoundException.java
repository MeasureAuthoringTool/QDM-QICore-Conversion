package gov.cms.mat.fhir.services.exceptions;

public class ThreadLocalNotFoundException extends RuntimeException {
    public ThreadLocalNotFoundException(String message) {
        super(message);
    }
}
