package gov.cms.mat.patients.conversion.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
// @JsonIgnoreProperties(ignoreUnknown = true)
public class BonniePatient {
    String _id;
    List<String> givenNames;
    String familyName;
    String bundleId;

    String[] provider_ids;

    List<ExpectedValues> expectedValues;

    String notes;

    @JsonProperty("measure_ids")
    List<String> measureIds;

    String user_id;

    QdmPatient qdmPatient;

    public String identifier() {
        return _id;
    }
}
