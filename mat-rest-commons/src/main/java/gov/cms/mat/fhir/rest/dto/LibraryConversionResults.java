package gov.cms.mat.fhir.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LibraryConversionResults {

    String matId;
    String reason;
    Boolean success;
    String link;
    String fhirLibraryJson;
    private List<FieldConversionResult> libraryResults = new ArrayList<>();
    private List<FhirValidationResult> libraryFhirValidationResults = new ArrayList<>();
    private CqlConversionResult cqlConversionResult = new CqlConversionResult();

    public LibraryConversionResults(String matId) {
        this.matId = matId;
    }
}
