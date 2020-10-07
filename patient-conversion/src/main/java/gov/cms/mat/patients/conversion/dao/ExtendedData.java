package gov.cms.mat.patients.conversion.dao;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
//@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtendedData {
    String type; // always null
    Boolean is_shared;
    OriginData[] origin_data;
    String test_id; // always null
    String medical_record_number;
    String medical_record_assigner; // always Bonnie
    String description; // always null
    String description_category; // always null

    /**
     * "insurance_providers": "[{\"author_datetime\":null,\"codes\":{\"SOP\":[\"349\"]},\"description\":null,\"end_time\":null,\"financial_responsibility_type\":{\"code\":\"SELF\",\"codeSystem\":\"HL7 Relationship Code\"},\"health_record_field\":null,\"member_id\":\"1234567890\",\"mood_code\":\"EVN\",\"name\":\"Other\",\"negationInd\":null,\"negationReason\":null,\"oid\":null,\"payer\":{\"name\":\"Other\"},\"reason\":null,\"relationship\":null,\"specifics\":null,\"start_time\":1199145600,\"status_code\":null,\"time\":null,\"type\":\"OT\"}]"
     */
    String insurance_providers;
}
