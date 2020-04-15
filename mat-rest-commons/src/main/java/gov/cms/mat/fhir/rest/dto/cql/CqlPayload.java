package gov.cms.mat.fhir.rest.dto.cql;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CqlPayload {
    String data;
    CqlPayloadType type;
}
