package gov.cms.mat.patients.conversion.dao;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
// @JsonIgnoreProperties(ignoreUnknown = true)
public class QdmPatient {
    SystemId _id;
    String qdmVersion;

    JsonDateTime birthDatetime;
    ExtendedData extendedData;

    List<DataElements> dataElements;
}
