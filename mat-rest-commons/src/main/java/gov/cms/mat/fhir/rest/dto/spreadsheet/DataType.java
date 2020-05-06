package gov.cms.mat.fhir.rest.dto.spreadsheet;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DataType {
    private String dataType;
    private String validValues;
    private String regex;
    private String type;
}
