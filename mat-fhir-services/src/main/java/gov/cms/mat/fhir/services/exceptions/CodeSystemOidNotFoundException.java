package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CodeSystemOidNotFoundException extends RuntimeException {
    private static final String MESSAGE = "Cannot find code system with oid: %s";

    public CodeSystemOidNotFoundException(String oid) {
        super(String.format(MESSAGE, oid));
        log.warn(getMessage());
    }
}
