package gov.cms.mat.fhir.services.components.reporting;

import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cms.mat.fhir.rest.dto.FieldConversionResult;
import gov.cms.mat.fhir.rest.dto.spreadsheet.MatAttribute;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
@Getter
public class MeasureResultMappingDto extends FieldConversionResult {
    private final MatAttribute matAttribute;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String errorMessage;

    MeasureResultMappingDto(FieldConversionResult result, MatAttribute matAttribute) {
        super(result.getField(), result.getDestination(), result.getReason());
        this.matAttribute = matAttribute;
        this.errorMessage = null;
    }

    MeasureResultMappingDto(FieldConversionResult result, String errorMessage) {
        super(result.getField(), result.getDestination(), result.getReason());
        this.matAttribute = null;
        this.errorMessage = errorMessage;
    }
}
