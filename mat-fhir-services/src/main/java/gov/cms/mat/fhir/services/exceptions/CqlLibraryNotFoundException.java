package gov.cms.mat.fhir.services.exceptions;

import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.summary.CqlLibraryFindData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
@Slf4j
public class CqlLibraryNotFoundException extends RuntimeException {
    private static final String NOT_FOUND_BY_MEASURE_ID = "Cannot find a CqlLibrary for measureId: %s";
    private static final String NOT_FOUND_BY_FIND_DATA = "Cannot find a CqlLibrary with: %s, %s, %s";

    public CqlLibraryNotFoundException(String measureId) {
        super(String.format(NOT_FOUND_BY_MEASURE_ID, measureId));
        log.warn(getMessage());
        ConversionReporter.setErrorMessage(getMessage());
    }

    public CqlLibraryNotFoundException(String message, String id) {
        super(message + " " + id);
        log.warn(getMessage());
    }

    public CqlLibraryNotFoundException(CqlLibraryFindData cqlLibraryFindData) {
        super(String.format(NOT_FOUND_BY_FIND_DATA, cqlLibraryFindData.getName(),
                cqlLibraryFindData.getQdmVersion(),
                cqlLibraryFindData.getVersion()));
        log.warn(getMessage());

    }
}
