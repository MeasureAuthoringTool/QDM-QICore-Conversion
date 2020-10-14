package gov.cms.mat.patients.conversion.dao;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Diagnoses {
    String _id;
    String _type;
    QdmCodeSystem code;
    QdmCode presentOnAdmissionIndicator;
    Integer rank;
    String qdmVersion;
}
