package gov.cms.mat.patients.conversion.dao;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OriginData {
    String patient_id;
    String[] measure_ids;
    String cms_id;
    String user_id;
    String user_email;
}
