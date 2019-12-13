package gov.cms.mat.fhir.services.summary;

import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.services.components.mongo.ConversionResult;
import gov.cms.mat.fhir.services.components.mongo.ConversionType;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class FhirLibraryResourceValidationResult extends FhirResourceValidationResult {

    private List<ConversionResult.FieldConversionResult> libraryResults = new ArrayList<>();
    private ConversionType libraryConversionType;

    @XmlElement
    public List<ConversionResult.FieldConversionResult> getLibraryResults() {
        return libraryResults;
    }

    public void setLibraryResults(List<ConversionResult.FieldConversionResult> libraryResults) {
        this.libraryResults = libraryResults;
    }

    @XmlElement
    public ConversionType getLibraryConversionType() {
        return libraryConversionType;
    }

    public void setLibraryConversionType(ConversionType libraryConversionType) {
        this.libraryConversionType = libraryConversionType;
    }
}
