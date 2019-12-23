package gov.cms.mat.fhir.rest.dto;

import lombok.Data;

@Data
public class CqlConversionBase {
    Integer startLine;
    Integer startChar;
    Integer endLine;
    Integer endChar;
    String errorType;
}
