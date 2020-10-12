package gov.cms.mat.patients.conversion.dao;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class QdmInterval {
    Date low;
    Date high;
    Boolean lowClosed;
    Boolean highClosed;
    String _type;
}
