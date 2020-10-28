package gov.cms.mat.fhir.rest.dto.cql;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CqlPayload {
    String data;
    CqlPayloadType type;
}
