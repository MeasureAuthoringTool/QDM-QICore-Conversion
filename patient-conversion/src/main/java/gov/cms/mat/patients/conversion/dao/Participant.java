package gov.cms.mat.patients.conversion.dao;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Participant {
    String _id;
    String qdmVersion;
    String _type;
    String hqmfOid;
    String qrdaOid;
    QdmCode relationship;

    QdmIdentifier identifier;

    String type;


}
