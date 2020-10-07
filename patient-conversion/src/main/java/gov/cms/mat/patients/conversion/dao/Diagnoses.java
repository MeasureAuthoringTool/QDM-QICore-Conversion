package gov.cms.mat.patients.conversion.dao;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Diagnoses {
    SystemId _id;
    String _type;
    QdmCodeSystem code;
    String presentOnAdmissionIndicator;
    Integer rank;
    String qdmVersion;
}
