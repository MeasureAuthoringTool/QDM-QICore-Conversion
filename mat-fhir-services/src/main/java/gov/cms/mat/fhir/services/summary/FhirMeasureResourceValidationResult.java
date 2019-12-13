package gov.cms.mat.fhir.services.summary;

import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.services.components.mongo.ConversionResult;
import gov.cms.mat.fhir.services.components.mongo.ConversionType;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class FhirMeasureResourceValidationResult extends FhirResourceValidationResult {

    private List<ConversionResult.FieldConversionResult> measureResults = new ArrayList<>();
    private ConversionType measureConversionType;

    @XmlElement
    public List<ConversionResult.FieldConversionResult> getMeasureResults() {
        return measureResults;
    }

    public void setMeasureResults(List<ConversionResult.FieldConversionResult> measureResults) {
        this.measureResults = measureResults;
    }

    @XmlElement
    public ConversionType getMeasureConversionType() {
        return measureConversionType;
    }

    public void setMeasureConversionType(ConversionType measureConversionType) {
        this.measureConversionType = measureConversionType;
    }
}
