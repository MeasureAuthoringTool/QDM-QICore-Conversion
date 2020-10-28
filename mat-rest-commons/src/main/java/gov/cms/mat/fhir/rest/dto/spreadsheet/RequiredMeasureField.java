package gov.cms.mat.fhir.rest.dto.spreadsheet;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RequiredMeasureField {
    private String field;
    private String type;
}
