package gov.cms.mat.fhir.rest.dto.spreadsheet;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HelpText {
    private String elementId;
    private String compositeHelp;
}
