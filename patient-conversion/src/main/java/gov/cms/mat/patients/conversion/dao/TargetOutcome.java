package gov.cms.mat.patients.conversion.dao;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TargetOutcome {
    String code;
    String system;
    String display;
    String version;
    String unit;
    Integer value;
}
