package gov.cms.mat.fhir.rest.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder
@Getter
@NoArgsConstructor
public class ConversionResultDto {
    private String measureId;
    private Instant modified;

    private ValueSetConversionResults valueSetConversionResults;

    private MeasureConversionResults measureConversionResults;

    private LibraryConversionResults libraryConversionResults;
}

