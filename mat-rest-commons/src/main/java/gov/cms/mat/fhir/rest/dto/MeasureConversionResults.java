package gov.cms.mat.fhir.rest.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MeasureConversionResults {
    private List<FieldConversionResult> measureResults = new ArrayList<>();
    private ConversionType measureConversionType;
    private List<FhirValidationResult> measureFhirValidationResults = new ArrayList<>();
}
