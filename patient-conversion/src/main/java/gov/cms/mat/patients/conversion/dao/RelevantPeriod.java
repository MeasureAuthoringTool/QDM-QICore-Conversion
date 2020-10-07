package gov.cms.mat.patients.conversion.dao;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RelevantPeriod {
    JsonDateTime low;
    JsonDateTime high;
    Boolean lowClosed;
    Boolean highClosed;
    String _type;
}
