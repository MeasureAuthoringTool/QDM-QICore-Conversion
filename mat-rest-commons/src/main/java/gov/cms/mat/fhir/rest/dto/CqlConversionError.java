package gov.cms.mat.fhir.rest.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class CqlConversionError extends CqlConversionBase {
    String errorSeverity;
    @EqualsAndHashCode.Exclude
    String targetIncludeLibraryId;
    @EqualsAndHashCode.Exclude
    String targetIncludeLibraryVersionId;
    String message;
    String type;
}
