package gov.cms.mat.fhir.rest.cql;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ValueSetResult {
    String oid;
    String reason;
    Boolean success;
}