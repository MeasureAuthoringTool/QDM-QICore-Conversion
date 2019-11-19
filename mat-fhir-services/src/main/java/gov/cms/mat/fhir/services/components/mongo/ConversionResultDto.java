package gov.cms.mat.fhir.services.components.mongo;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ConversionResultDto {
    private String measureId;

    private List<ConversionResult.ValueSetResult> valueSetResults;

    private List<MeasureResultMappingDto> measureResults;

    private List<ConversionResult.LibraryResult> libraryResults;
}
