package gov.cms.mat.fhir.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CqlConversionReportError extends CqlConversionBase {
    String errorSeverity;

    String libraryId;

    String libraryVersion; //added to

    String message;
    String type;

    int count;

}
