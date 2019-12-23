package gov.cms.mat.fhir.rest.cql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class FieldConversionResult {
    String field;
    String destination;
    String reason;


}
