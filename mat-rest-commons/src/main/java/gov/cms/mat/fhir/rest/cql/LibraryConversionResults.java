package gov.cms.mat.fhir.rest.cql;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LibraryConversionResults {
    private List<FieldConversionResult> libraryResults = new ArrayList<>();
    private ConversionType libraryConversionType;
    private List<FhirValidationResult> libraryFhirValidationErrors = new ArrayList<>();
    private CqlConversionResult cqlConversionResult;
}
