package gov.cms.mat.patients.conversion.dao;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hl7.fhir.r4.model.Identifier;

@Data
@NoArgsConstructor
public class Sender {
    String _id;
    String qdmVersion;
    String _type;

    QdmCode role;
    QdmCode specialty;
    QdmCode qualification;

    QdmIdentifier identifier;
    String hqmfOid;
    String qrdaOid;


}
