package gov.cms.mat.fhir.services.components.conversion;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ConversionDataTypeResult {
    String type;
    String comment;
    String whereAdjustment;
}
