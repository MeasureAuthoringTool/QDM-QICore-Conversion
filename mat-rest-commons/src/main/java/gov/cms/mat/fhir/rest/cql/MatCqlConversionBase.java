package gov.cms.mat.fhir.rest.cql;

import lombok.Data;

@Data
public class MatCqlConversionBase {
    Integer startLine;
    Integer startChar;
    Integer endLine;
    Integer endChar;
    String errorType;
}
