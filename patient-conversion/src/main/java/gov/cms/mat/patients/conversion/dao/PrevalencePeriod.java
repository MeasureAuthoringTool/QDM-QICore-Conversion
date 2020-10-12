package gov.cms.mat.patients.conversion.dao;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class PrevalencePeriod {
    Date low;
    Date high;
    Boolean lowClosed;
    Boolean highClosed;
    String _type;
}
