package gov.cms.mat.patients.conversion.dao;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
// @JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QdmDataElement {
    String _id;
    List<QdmCodeSystem> dataElementCodes;

    String _type;

    QdmCodeSystem type; //in there  "_id": "5c95406eb8484612c37f1f57",

    String facilityLocation;

    List<FacilityLocation> facilityLocations;
    String qdmTitle;
    String hqmfOid;
    String qrdaOid;
    String qdmCategory;
    String qdmStatus;
    String qdmVersion;

    QdmInterval participationPeriod;

    // ids of other patients?
    /* "relatedTo": [
          "5c7592f1b8484660416e290c"
        ] */
    List<String> relatedTo;


    Date expiredDatetime;
    Date activeDatetime;
    Date birthDatetime;

    Date authorDatetime;
    Sender sender;
    Sender recipient;

    Integer refills;
    QdmQuantity dosage;
    QdmQuantity supply;
    QdmCodeSystem frequency;
    Integer daysSupplied;
    QdmCodeSystem setting;
    QdmCodeSystem route;

    QdmCodeSystem admissionSource;

    QdmPeriod relevantPeriod;

    QdmPeriod prevalencePeriod;

    QdmCodeSystem dischargeDisposition;


    List<Diagnoses> diagnoses;

    QdmCodeSystem reason;  //"5ca62964b8484628b8de1f51", is a Qdm

   // @JsonIgnore  // "5aeb772fb848463d625b1dd7" is an int
    JsonNode result;

    QdmReferenceRange referenceRange;

    QdmCodeSystem status;
    Date resultDatetime;
    QdmCodeSystem method;

    QdmCodeSystem negationRationale;

    QdmCodeSystem priority;

    Participant participant;

    LengthOfStay lengthOfStay;
    QdmCodeSystem anatomicalLocationSite;
    QdmCodeSystem severity;
    QdmCodeSystem relationship;

    Date incisionDatetime;

    List<QdmComponent> components;

    String description;

    /* "targetOutcome": {
          "unit": "",
          "value": 2
        } */
    //@JsonIgnore
  //  QdmCode targetOutcome;  // "_id": "5d654ae61c76ba7ea32ed30c",
    //JsonNode targetOutcome;

   TargetOutcome targetOutcome;

    String codeListId;
    Prescriber prescriber;

    Date relevantDatetime;

    String rank;

    QdmCodeSystem category;
    QdmCodeSystem medium;
    Date sentDatetime;
    Date receivedDatetime;

    Prescriber dispenser;
    Prescriber performer;

    public String identifier() {
        return _id;
    }
}
