package gov.cms.mat.fhir.rest.dto;



import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@Data
public class ValueSetValidationResult {
    @NonNull
    final String oid;
    List<FhirValidationResult> valueSetFhirValidationResults = new ArrayList<>();
}
