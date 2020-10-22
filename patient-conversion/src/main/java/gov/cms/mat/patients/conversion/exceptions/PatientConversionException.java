package gov.cms.mat.patients.conversion.exceptions;

public class PatientConversionException extends RuntimeException {
    public PatientConversionException(String message) {
        super(message);
    }

    public PatientConversionException(String error, Exception e) {
        super(error, e);
    }
}
