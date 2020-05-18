package gov.cms.mat.cql.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class CqlConversionPayload {
    @Setter
    String json;
    String xml;
}
