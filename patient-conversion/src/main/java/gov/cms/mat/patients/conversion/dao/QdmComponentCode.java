package gov.cms.mat.patients.conversion.dao;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QdmComponentCode {
    String code;
    String version;
    String descriptor;
    String system;

    String display;
}
