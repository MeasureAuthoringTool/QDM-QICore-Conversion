package gov.cms.mat.fhir.services.summary;

import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.rest.dto.LibraryConversionResults;


import java.util.ArrayList;
import java.util.List;

public class FhirLibraryResourceValidationResult extends FhirResourceValidationResult {

    List<LibraryConversionResults> libraryConversionResults = new ArrayList<>();
    private ConversionType libraryConversionType;

    public FhirLibraryResourceValidationResult(String id) {
        super(id, "Library");
    }


    public List<LibraryConversionResults> getLibraryConversionResults() {
        return libraryConversionResults;
    }

    public void setLibraryConversionResults(List<LibraryConversionResults> libraryConversionResults) {
        this.libraryConversionResults = libraryConversionResults;
    }


    public ConversionType getLibraryConversionType() {
        return libraryConversionType;
    }

    public void setLibraryConversionType(ConversionType libraryConversionType) {
        this.libraryConversionType = libraryConversionType;
    }
}
