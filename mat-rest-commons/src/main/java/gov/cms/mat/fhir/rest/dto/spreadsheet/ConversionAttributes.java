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
public class ConversionAttributes {
    private String qdmType;
    private String qdmAttribute;
    private String fhirType;
    private String fhirAttribute;
    private String comment;
}

