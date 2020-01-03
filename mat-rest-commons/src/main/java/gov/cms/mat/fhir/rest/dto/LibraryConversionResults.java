package gov.cms.mat.fhir.rest.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LibraryConversionResults {
    private List<FieldConversionResult> libraryResults = new ArrayList<>();
    private ConversionType libraryConversionType;
    private List<FhirValidationResult> libraryFhirValidationResults = new ArrayList<>();
    private CqlConversionResult cqlConversionResult;
}
