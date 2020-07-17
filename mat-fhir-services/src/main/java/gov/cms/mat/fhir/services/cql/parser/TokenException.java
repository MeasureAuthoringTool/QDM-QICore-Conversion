package gov.cms.mat.fhir.services.cql.parser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TokenException extends RuntimeException {
    private static final String MESSAGE = "At token index: %d Expected token type: %d found: %d";

    public TokenException(int index, int expectedType, int tokenType) {
        super(String.format(MESSAGE, index, expectedType, tokenType));
        log.error(this.getMessage(), this);
    }
}
