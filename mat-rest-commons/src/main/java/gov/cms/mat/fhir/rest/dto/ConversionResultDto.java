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

    List<ValueSetConversionResults> valueSetConversionResults;

    MeasureConversionResults measureConversionResults;

    List<LibraryConversionResults> libraryConversionResults;
}

