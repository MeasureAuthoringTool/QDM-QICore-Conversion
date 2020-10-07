package gov.cms.mat.patients.conversion.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
// @JsonIgnoreProperties(ignoreUnknown = true)
public class DataElements {
    SystemId _id;
    List<QdmCodeSystem> dataElementCodes;

    @JsonProperty("_type")
    String type;
    String[] facilityLocations;
    String qdmTitle;
    String hqmfOid;
    String qdmCategory;
    String qdmStatus;
    String qdmVersion;
    JsonDateTime expiredDatetime;

    JsonDateTime birthDatetime;
    JsonDateTime authorDatetime;
    String refills;
    String dosage;
    String supply;
    String frequency;
    Integer daysSupplied;
    String route;

    String admissionSource;

    RelevantPeriod relevantPeriod;
    QdmCodeSystem dischargeDisposition;
    List<Diagnoses> diagnoses;
    String reason;
    String result;
    String status;
    QdmCodeSystem negationRationale;

    String priority;

    LengthOfStay lengthOfStay;

    String description;
    String codeListId;

    public String identifier() {
        return _id.getOid();
    }
}
