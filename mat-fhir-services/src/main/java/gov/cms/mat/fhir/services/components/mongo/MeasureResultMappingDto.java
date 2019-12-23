package gov.cms.mat.fhir.services.components.mongo;

import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cms.mat.fhir.rest.dto.ConversionMapping;
import gov.cms.mat.fhir.rest.dto.FieldConversionResult;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
@Getter
public class MeasureResultMappingDto extends FieldConversionResult {
    private final ConversionMapping conversionMapping;

    @JsonInclude(JsonInclude.Include.NON_NULL)
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
