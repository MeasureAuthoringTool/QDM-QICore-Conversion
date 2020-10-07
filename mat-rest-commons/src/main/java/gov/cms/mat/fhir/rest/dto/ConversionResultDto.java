package gov.cms.mat.fhir.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversionResultDto {
    String measureId;
    String modified;

    private String errorReason;
    private ConversionOutcome outcome;
    private ConversionType conversionType;

    MeasureConversionResults measureConversionResults;

    List<LibraryConversionResults> libraryConversionResults;
}

