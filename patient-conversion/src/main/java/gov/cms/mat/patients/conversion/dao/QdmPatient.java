package gov.cms.mat.patients.conversion.dao;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
// @JsonIgnoreProperties(ignoreUnknown = true)
public class QdmPatient {
    String _id;
    String qdmVersion;

    Date birthDatetime;
    ExtendedData extendedData;
   // JsonNode extendedData;

    List<QdmDataElement> dataElements;
}
