package gov.cms.mat.patients.conversion.dao;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LengthOfStay {
    Integer value;
    String unit;
    String _type;
}
