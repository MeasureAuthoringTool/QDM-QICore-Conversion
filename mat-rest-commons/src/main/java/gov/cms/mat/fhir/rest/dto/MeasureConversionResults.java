package gov.cms.mat.fhir.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MeasureConversionResults {
    private List<FieldConversionResult> measureResults = new ArrayList<>();
    private ConversionType measureConversionType;
    private List<FhirValidationResult> measureFhirValidationResults = new ArrayList<>();

    String reason;
    Boolean success;
    String link;

    String fhirMeasureJson;
}
