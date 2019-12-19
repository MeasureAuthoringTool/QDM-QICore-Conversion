package gov.cms.mat.fhir.services.service.support;

import lombok.Data;

@Data
public class CqlConversionBase {
    Integer startLine;
    Integer startChar;
    Integer endLine;
    Integer endChar;
    String errorType;
}
