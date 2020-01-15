package gov.cms.mat.fhir.services.components.mongo;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class ConversionKey {
    private final String measureId;
    private final Instant start;
}
