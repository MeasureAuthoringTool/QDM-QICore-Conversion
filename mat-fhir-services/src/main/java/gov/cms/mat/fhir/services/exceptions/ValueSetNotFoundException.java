package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
@Slf4j
public class ValueSetNotFoundException extends RuntimeException {
    private static final String NOT_FOUND_BY_OID = "Cannot find a ValueSet with oid: %s";

    public ValueSetNotFoundException(String oid) {
        super(String.format(NOT_FOUND_BY_OID, oid));
        log.warn(getMessage());
    }


}
