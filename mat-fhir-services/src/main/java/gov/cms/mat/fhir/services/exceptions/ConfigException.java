package gov.cms.mat.fhir.services.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigException extends RuntimeException {
    private static final String MESSAGE = "Path %s %s";

    public ConfigException(String directoryName, String reason) {
        super(String.format(MESSAGE, directoryName, reason));
        log.warn(getMessage());
    }
}
