package gov.cms.mat.fhir.rest.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class CqlConversionError extends CqlConversionBase {
    String errorSeverity;
    String targetIncludeLibraryId;
    String targetIncludeLibraryVersionId;
    String type;
    String message;
}
