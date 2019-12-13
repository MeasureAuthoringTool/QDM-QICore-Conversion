package gov.cms.mat.fhir.services.components.mongo;

import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cms.mat.fhir.services.service.support.ConversionMapping;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
@Getter
public class MeasureResultMappingDto extends ConversionResult.FieldConversionResult {
    private final ConversionMapping conversionMapping;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String errorMessage;

    MeasureResultMappingDto(ConversionResult.FieldConversionResult result, ConversionMapping conversionMapping) {
        super(result.field, result.destination, result.reason);
        this.conversionMapping = conversionMapping;
        this.errorMessage = null;
    }

    MeasureResultMappingDto(ConversionResult.FieldConversionResult result, String errorMessage) {
        super(result.field, result.destination, result.reason);
        this.conversionMapping = null;
        this.errorMessage = errorMessage;
    }
}
