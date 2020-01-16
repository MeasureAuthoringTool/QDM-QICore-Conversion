package gov.cms.mat.fhir.services.components.mongo;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

@Builder
@Getter
@ToString
public class ConversionKey {
    private final String measureId;
    private final Instant start;
}
