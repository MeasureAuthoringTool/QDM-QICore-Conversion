package gov.cms.mat.fhir.rest.cql;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MeasureConversionResults {
    private List<FieldConversionResult> measureResults = new ArrayList<>();
    private ConversionType measureConversionType;
    private List<FhirValidationResult> measureFhirValidationErrors = new ArrayList<>();
}
