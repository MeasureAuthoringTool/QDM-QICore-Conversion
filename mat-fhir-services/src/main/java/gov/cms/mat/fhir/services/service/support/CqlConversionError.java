package gov.cms.mat.fhir.services.service.support;

import lombok.Data;

@Data
public class CqlConversionError {
    Integer startLine;
    Integer startChar;
    Integer endLine;
    Integer endChar;
    String message;
    String errorType;
    String errorSeverity;
    String targetIncludeLibraryId;
    String targetIncludeLibraryVersionId;
    String type;
}
