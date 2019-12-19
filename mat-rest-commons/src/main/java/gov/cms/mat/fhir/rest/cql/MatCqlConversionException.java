package gov.cms.mat.fhir.rest.cql;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MatCqlConversionException extends MatCqlConversionBase {
    String errorSeverity;
    String targetIncludeLibraryId;
    String targetIncludeLibraryVersionId;
    String type;
    String message;
}
