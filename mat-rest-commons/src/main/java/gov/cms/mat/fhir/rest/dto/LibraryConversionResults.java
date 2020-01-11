package gov.cms.mat.fhir.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hl7.fhir.r4.model.Library;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LibraryConversionResults {

    String matId;

    private List<FieldConversionResult> libraryResults = new ArrayList<>();

    private List<FhirValidationResult> libraryFhirValidationResults = new ArrayList<>();
    String reason;
    Boolean success;
    String link;
    private CqlConversionResult cqlConversionResult = new CqlConversionResult();

    public LibraryConversionResults(String matId) {
        this.matId = matId;
    }

    Library fhirLibrary;
}
