package gov.cms.mat.patients.conversion.dao;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Prescriber {
    String _id;
    String qdmVersion;
    String _type;
    String hqmfOid;
    String qrdaOid;
    QdmIdentifier identifier;

    String role;
    String specialty;
    String qualification;
    String type;
}
