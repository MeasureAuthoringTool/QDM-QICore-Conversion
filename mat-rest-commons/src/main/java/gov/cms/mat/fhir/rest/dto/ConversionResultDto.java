package gov.cms.mat.fhir.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversionResultDto {
    private String measureId;
    private String modified;

    private ValueSetConversionResults valueSetConversionResults;

    private MeasureConversionResults measureConversionResults;

    private LibraryConversionResults libraryConversionResults;
}

