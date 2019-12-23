package gov.cms.mat.fhir.rest.cql;

import lombok.Data;

@Data
public class CqlConversionBase {
    Integer startLine;
    Integer startChar;
    Integer endLine;
    Integer endChar;
    String errorType;
}
