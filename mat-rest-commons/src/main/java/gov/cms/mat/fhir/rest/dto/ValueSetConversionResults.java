package gov.cms.mat.fhir.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValueSetConversionResults {
    String oid;
    String reason;
    Boolean success;
    String link;
    String json;

    List<FhirValidationResult> valueSetFhirValidationResults = new ArrayList<>();

    public ValueSetConversionResults(String oid) {
        this.oid = oid;
    }
}
