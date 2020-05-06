package gov.cms.mat.fhir.rest.dto;


import gov.cms.mat.fhir.rest.dto.spreadsheet.MatAttribute;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
@Getter
public class MeasureResultMappingDto extends FieldConversionResult {
    private final MatAttribute matAttributes;

    private final String errorMessage;

    MeasureResultMappingDto(FieldConversionResult result, MatAttribute conversionMapping) {
        super(result.getField(), result.getDestination(), result.getReason());
        this.matAttributes = conversionMapping;
        this.errorMessage = null;
    }

    MeasureResultMappingDto(FieldConversionResult result, String errorMessage) {
        super(result.getField(), result.getDestination(), result.getReason());
        this.matAttributes = null;
        this.errorMessage = errorMessage;
    }
}
