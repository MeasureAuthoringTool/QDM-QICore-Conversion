package gov.cms.mat.fhir.rest.dto.spreadsheet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@EqualsAndHashCode
public class QdmToQicoreMapping {
    private String title;
    private String matDataType;
    private String matAttributeType;
    private String fhirQICoreMapping;
    private String type;
    private String cardinality;
}

