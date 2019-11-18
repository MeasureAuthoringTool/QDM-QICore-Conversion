package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QdmQiCoreDataException extends RuntimeException {
    public QdmQiCoreDataException(String message) {
        super(message);
        log.warn(message);
    }
}
