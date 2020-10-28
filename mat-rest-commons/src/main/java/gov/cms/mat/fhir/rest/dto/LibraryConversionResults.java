package gov.cms.mat.fhir.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LibraryConversionResults {
    String matLibraryId;
    String fhirLibraryId;
    String name;

    String version;
    String reason;
    Boolean success;
    String link;
    String fhirLibraryJson;
    Map<String, List<CqlConversionError>> externalErrors;
    /* Error results when validating the  */
    private List<FhirValidationResult> libraryFhirValidationResults = new ArrayList<>(); // KEEP
    private CqlConversionResult cqlConversionResult = new CqlConversionResult();

    public LibraryConversionResults(String matLibraryId) {
        this.matLibraryId = matLibraryId;
    }
}
