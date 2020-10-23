package gov.cms.mat.patients.conversion.dao;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
// @JsonIgnoreProperties(ignoreUnknown = true)
public class ExtendedData {
    String type; // always null
    Boolean is_shared;

    //  OriginData[] origin_data;
    JsonNode origin_data;

    String test_id; // always null
    String medical_record_number;
    String medical_record_assigner; // always Bonnie
    String description; // always null
    String description_category; // always null

    /**
     * "insurance_providers": "[{\"author_datetime\":null,\"codes\":{\"SOP\":[\"349\"]},\"description\":null,\"end_time\":null,\"financial_responsibility_type\":{\"code\":\"SELF\",\"codeSystem\":\"HL7 Relationship Code\"},\"health_record_field\":null,\"member_id\":\"1234567890\",\"mood_code\":\"EVN\",\"name\":\"Other\",\"negationInd\":null,\"negationReason\":null,\"oid\":null,\"payer\":{\"name\":\"Other\"},\"reason\":null,\"relationship\":null,\"specifics\":null,\"start_time\":1199145600,\"status_code\":null,\"time\":null,\"type\":\"OT\"}]"
     */
    String insurance_providers;

    JsonNode source_data_criteria; // what to do with data below
   /* source_data_criteria": [
        {
          "negation": false,
          "definition": "medication",
          "status": "discharge",
          "title": "ScheduleIVBenzodiazepines",
          "description": "Medication, Discharge: Schedule IV Benzodiazepines",
          "code_list_id": "2.16.840.1.113762.1.4.1125.1",
          "type": "medications",
          "id": "ScheduleIVBenzodiazepines_MedicationDischarge_965986d3_08b3_4245_9f2b_0a51483a6440_source",
          "start_date": 1330588800000,
          "value": null,
          "references": null,
          "hqmf_set_id": "33B40C00-909A-4490-8093-999FBCDC3480",
          "cms_id": "CMS506v2",
          "criteria_id": "170ac55ffef9k",
          "codes": {
            "RxNorm": [
              "312135"
            ]
          },
          "negation_code_list_id": "",
          "coded_entry_id": "5ed80363a2f916522425e94f",
          "code_source": "USER_DEFINED"
        },
        {
          "negation": false,
          "definition": "encounter",
          "status": "performed",
          "title": "EncounterInpatient",
          "description": "Encounter, Performed: Encounter Inpatient",
          "code_list_id": "2.16.840.1.113883.3.666.5.307",
          "type": "encounters",
          "id": "EncounterInpatient_EncounterPerformed_5b2d5be7_afe0_42fb_97ae_017964019fd5_source",
          "start_date": 1330588800000,
          "end_date": 1330784100000,
          "value": null,
          "references": null,
          "hqmf_set_id": "33B40C00-909A-4490-8093-999FBCDC3480",
          "cms_id": "CMS506v2",
          "criteria_id": "170ac55ae536Q",
          "codes": {
            "SNOMED-CT": [
              "183452005"
            ]
          }*/
}
