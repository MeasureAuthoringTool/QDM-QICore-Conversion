package gov.cms.mat.fhir.rest.cql;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class ValueSetValidationResult {
    @NonNull
    final String oid;
    List<FhirValidationResult> libraryFhirValidationErrors = new ArrayList<>();
}
