package gov.cms.mat.fhir.services.exceptions;

import gov.cms.mat.fhir.services.components.reporting.ConversionReporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static gov.cms.mat.fhir.rest.dto.ConversionOutcome.CQLLIBRARY_NOT_FOUND;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
@Slf4j
public class NoCqlLibrariesFoundException extends RuntimeException {
    private static final String NOT_FOUND_BY_MEASURE_ID = "Cannot find any CqlLibraries for measureId: %s";


    public NoCqlLibrariesFoundException(String measureId) {
        super(String.format(NOT_FOUND_BY_MEASURE_ID, measureId));
        ConversionReporter.setTerminalMessage(getMessage(), CQLLIBRARY_NOT_FOUND);
        log.warn(getMessage());
    }
}
