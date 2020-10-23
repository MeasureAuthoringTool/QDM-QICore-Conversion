package gov.cms.mat.patients.conversion.dao;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QdmReferenceRange {
    QdmQuantity low;
    QdmQuantity high;
    Boolean lowClosed;
    Boolean highClosed;
    String _type; //  is "QDM::Interval"  but has different high/low
}
