package gov.cms.mat.cql.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QdmMappingException extends RuntimeException {
    private static final String ERROR_MESSAGE = "Cannot find any mappings for matDataTypeDescription: %s";

    public QdmMappingException(String matDataTypeDescription) {
        super(String.format(ERROR_MESSAGE, matDataTypeDescription));
        log.warn(getMessage());
    }
}
