package gov.cms.mat.fhir.rest.dto;

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
    List<FhirValidationResult> fhirValidationResults = new ArrayList<>();
}
