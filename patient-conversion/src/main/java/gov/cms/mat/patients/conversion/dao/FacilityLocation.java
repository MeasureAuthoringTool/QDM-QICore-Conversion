package gov.cms.mat.patients.conversion.dao;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FacilityLocation {

    String qdmVersion;
    String _type;
    String _id;
    QdmCodeSystem code;

    QdmInterval locationPeriod;
}
