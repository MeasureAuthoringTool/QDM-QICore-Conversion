package gov.cms.mat.fhir.rest.cql;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ValueSetConversionResults {
    private List<ValueSetResult> valueSetResults = new ArrayList<>();
    private ConversionType valueSetConversionType;
    private List<ValueSetValidationResult> valueSetFhirValidationErrors = new ArrayList<>();
}
