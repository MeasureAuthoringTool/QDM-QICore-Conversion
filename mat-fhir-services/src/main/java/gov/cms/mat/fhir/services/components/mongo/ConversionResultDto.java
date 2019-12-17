package gov.cms.mat.fhir.services.components.mongo;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
public class ConversionResultDto {
    private String measureId;

    private Instant modified;

    private List<ConversionResult.ValueSetResult> valueSetResults;
    private ConversionType valueSetConversionType;
    private List<ConversionResult.ValueSetValidationResult> valueSetFhirValidationErrors;

    private List<MeasureResultMappingDto> measureResults;
    private ConversionType measureConversionType;
    private List<ConversionResult.FhirValidationResult> measureFhirValidationErrors;


    private List<ConversionResult.FieldConversionResult> libraryResults;
    private ConversionType libraryConversionType;
    private List<ConversionResult.FhirValidationResult> libraryFhirValidationErrors = new ArrayList<>();

    private ConversionResult.CqlConversionResult cqlConversionResult;

}
