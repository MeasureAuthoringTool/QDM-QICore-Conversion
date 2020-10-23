package gov.cms.mat.patients.conversion.dao;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Sender {
    String _id;
    String qdmVersion;
    String _type;

    QdmCodeSystem role;
    QdmCodeSystem specialty;
    QdmCodeSystem qualification;

    QdmIdentifier identifier;
    String hqmfOid;
    String qrdaOid;


}
