package gov.cms.mat.patients.conversion.dao;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QdmComponentResult {
    String code;
    String version;
    String descriptor;
    String system;

    String unit;
    String value;
}
