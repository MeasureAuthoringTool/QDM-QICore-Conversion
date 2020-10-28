package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HapiCreateException extends RuntimeException {
    public HapiCreateException(String s) {
        super(s);
        log.debug(getMessage(), this);
    }
}
