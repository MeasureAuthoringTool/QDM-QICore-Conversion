package gov.cms.mat.fhir.rest.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
public class ValueSetConversionResults {
    private List<ValueSetResult> valueSetResults = new ArrayList<>();
    private ConversionType valueSetConversionType;
    private List<ValueSetValidationResult> valueSetFhirValidationResults = new ArrayList<>();
}
