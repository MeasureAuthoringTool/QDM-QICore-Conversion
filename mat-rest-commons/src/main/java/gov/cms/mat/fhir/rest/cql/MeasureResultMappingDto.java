package gov.cms.mat.fhir.rest.cql;


import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
@Getter
public class MeasureResultMappingDto extends FieldConversionResult {
    private final ConversionMapping conversionMapping;

    private final String errorMessage;

    MeasureResultMappingDto(FieldConversionResult result, ConversionMapping conversionMapping) {
        super(result.getField(), result.getDestination(), result.getReason());
        this.conversionMapping = conversionMapping;
        this.errorMessage = null;
    }

    MeasureResultMappingDto(FieldConversionResult result, String errorMessage) {
        super(result.getField(), result.getDestination(), result.getReason());
        this.conversionMapping = null;
        this.errorMessage = errorMessage;
    }
}
