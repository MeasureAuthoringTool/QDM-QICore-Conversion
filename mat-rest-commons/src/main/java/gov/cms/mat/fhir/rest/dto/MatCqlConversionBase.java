package gov.cms.mat.fhir.rest.dto;

import lombok.Data;

@Data
public class MatCqlConversionBase {
    Integer startLine;
    Integer startChar;
    Integer endLine;
    Integer endChar;
    String errorType;
}
