package gov.cms.mat.patients.conversion.dao;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QdmIdentifier {
    String _id;
    String _type;
    String qdmVersion;
    String namingSystem;
    String value;
}
