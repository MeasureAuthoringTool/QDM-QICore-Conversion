package gov.cms.mat.fhir.rest.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class CqlConversionBase {
    Integer startLine;
    Integer startChar;
    Integer endLine;
    Integer endChar;
    @EqualsAndHashCode.Exclude
    String errorType;
}
