package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MatXmlMarshalException extends RuntimeException {
    public MatXmlMarshalException(String message) {
        super(message);
        log.warn(message);
    }

    public MatXmlMarshalException(Exception e) {
        super(e);
        log.warn(e.getMessage());
    }
}
