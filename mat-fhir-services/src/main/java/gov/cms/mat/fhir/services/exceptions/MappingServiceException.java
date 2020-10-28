package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MappingServiceException extends RuntimeException {
    public MappingServiceException(String message) {
        super(message);
        log.warn(message);
    }
}
