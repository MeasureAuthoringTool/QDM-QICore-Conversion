package gov.cms.mat.fhir.rest.dto.spreadsheet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@EqualsAndHashCode
public class ConversionDataTypes {
    private String qdmType;
    private String fhirType;
    private String whereAdjustment;
    private String comment;
}

