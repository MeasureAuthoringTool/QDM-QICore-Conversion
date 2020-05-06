package gov.cms.mat.fhir.services.exceptions;

import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.summary.CqlLibraryFindData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.CQLLIBRARY_NOT_FOUND;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
@Slf4j
public class CqlLibraryNotFoundException extends RuntimeException {
    private static final String NOT_FOUND_BY_MEASURE_ID = "Cannot find a CqlLibrary for measureId: %s";
    private static final String NOT_FOUND_BY_FIND_DATA = "Cannot find a %s CqlLibrary with: %s, " +
            "QdmVersion: %s, MatVersion: %s";

    private static final String NOT_FOUND_BY_MEASURE_ID_VERSION_LIBRARY = NOT_FOUND_BY_MEASURE_ID + " library: %s and version: %s";

    public CqlLibraryNotFoundException(String measureId) {
        super(String.format(NOT_FOUND_BY_MEASURE_ID, measureId));
        ConversionReporter.setTerminalMessage(getMessage(), CQLLIBRARY_NOT_FOUND);
        log.warn(getMessage());
    }

    public CqlLibraryNotFoundException(String measureId, String library, String version) {
        super(String.format(NOT_FOUND_BY_MEASURE_ID_VERSION_LIBRARY, measureId, library, version));
        ConversionReporter.setTerminalMessage(getMessage(), CQLLIBRARY_NOT_FOUND);
        log.warn(getMessage());
    }

    public CqlLibraryNotFoundException(String message, String id) {
        super(message + " " + id);
        log.warn(getMessage());
    }

    public CqlLibraryNotFoundException(CqlLibraryFindData cqlLibraryFindData) {
        super(String.format(NOT_FOUND_BY_FIND_DATA,
                cqlLibraryFindData.getType(),
                cqlLibraryFindData.getName(),
                cqlLibraryFindData.getQdmVersion(),
                cqlLibraryFindData.getMatVersion()));
        log.warn(getMessage());
    }
}
