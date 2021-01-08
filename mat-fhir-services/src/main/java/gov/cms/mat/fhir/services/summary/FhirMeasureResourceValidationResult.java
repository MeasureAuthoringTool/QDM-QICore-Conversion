package gov.cms.mat.fhir.services.summary;

import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.rest.dto.FieldConversionResult;


import java.util.ArrayList;
import java.util.List;

public class FhirMeasureResourceValidationResult extends FhirResourceValidationResult {

    private List<FieldConversionResult> measureResults = new ArrayList<>();
    private ConversionType measureConversionType;

    public FhirMeasureResourceValidationResult() {
        super();
    }

    public FhirMeasureResourceValidationResult(String id, String measure) {
        super(id, measure);
    }


    public List<FieldConversionResult> getMeasureResults() {
        return measureResults;
    }

    public void setMeasureResults(List<FieldConversionResult> measureResults) {
        this.measureResults = measureResults;
    }


    public ConversionType getMeasureConversionType() {
        return measureConversionType;
    }

    public void setMeasureConversionType(ConversionType measureConversionType) {
        this.measureConversionType = measureConversionType;
    }
}
