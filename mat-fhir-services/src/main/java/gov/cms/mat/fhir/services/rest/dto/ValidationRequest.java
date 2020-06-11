package gov.cms.mat.fhir.services.rest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;

@Data
@NoArgsConstructor
@Nullable
public class ValidationRequest {
    private int timeoutSeconds = -1;
    private boolean validateValueSets = true;
    private boolean validateCodeSystems = true;
    private boolean validateSyntax = true;
    private boolean validateCqlToElm = true;
}
