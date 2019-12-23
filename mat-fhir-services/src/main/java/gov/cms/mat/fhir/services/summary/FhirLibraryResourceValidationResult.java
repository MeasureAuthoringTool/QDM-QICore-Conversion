package gov.cms.mat.fhir.services.summary;

import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.rest.dto.FieldConversionResult;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class FhirLibraryResourceValidationResult extends FhirResourceValidationResult {

    private List<FieldConversionResult> libraryResults = new ArrayList<>();
    private ConversionType libraryConversionType;

    public FhirLibraryResourceValidationResult(String id) {
        super(id, "Library");
    }

    @XmlElement
    public List<FieldConversionResult> getLibraryResults() {
        return libraryResults;
    }

    public void setLibraryResults(List<FieldConversionResult> libraryResults) {
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
