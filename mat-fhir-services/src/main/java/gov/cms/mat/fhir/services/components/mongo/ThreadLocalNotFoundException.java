package gov.cms.mat.fhir.services.components.mongo;

public class ThreadLocalNotFoundException extends RuntimeException {
    public ThreadLocalNotFoundException(String message) {
        super(message);
    }
}
