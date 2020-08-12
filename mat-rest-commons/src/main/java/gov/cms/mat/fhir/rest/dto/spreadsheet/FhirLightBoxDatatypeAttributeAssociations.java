package gov.cms.mat.fhir.rest.dto.spreadsheet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class FhirLightBoxDatatypeAttributeAssociations  implements Comparable<FhirLightBoxDatatypeAttributeAssociations> {
    private String datatype;
    private String attribute;
    private String attributeType;
    private Boolean hasBinding;

    @Override
    public int compareTo(FhirLightBoxDatatypeAttributeAssociations rhs) {
        int last = this.datatype.compareTo(rhs.datatype);
        return last == 0 ? this.attribute.compareTo(rhs.attribute) : last;
    }
}

