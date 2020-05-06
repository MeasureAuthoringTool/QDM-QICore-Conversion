package gov.cms.mat.fhir.rest.dto.spreadsheet;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MatAttribute {
    private String dataTypeDescription;
    private String matAttributeName;
    private String fhirQicoreMapping;
    private String fhirResource;
    private String fhirType;
    private String fhirElement;
    private String helpWording;
    private List<String> dropDown;
}
