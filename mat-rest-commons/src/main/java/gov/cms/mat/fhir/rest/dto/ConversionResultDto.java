package gov.cms.mat.fhir.rest.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Builder
@Getter
@Setter
@NoArgsConstructor
public class ConversionResultDto {
    private String measureId;
    private Instant modified;

    private ValueSetConversionResults valueSetConversionResults;

    private MeasureConversionResults measureConversionResults;

    private LibraryConversionResults libraryConversionResults;
}

