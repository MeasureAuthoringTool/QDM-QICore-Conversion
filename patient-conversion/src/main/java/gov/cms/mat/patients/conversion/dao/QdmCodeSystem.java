package gov.cms.mat.patients.conversion.dao;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QdmCodeSystem {
    String code;
    String system;
    String display;
    String version;
    String _type;
}
