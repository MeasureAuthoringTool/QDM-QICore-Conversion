/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.rest.packaging;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.hl7.fhir.r4.model.Bundle;

/**
 *
 * @author duanedecouteau
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"packagingOutcome", "measurePackage", "libraryPackage"})
public class FhirPackage {
    @JsonProperty("measurePackage")
    private String measurePackage;
    @JsonProperty("libraryPackage")
    private String libraryPackage;
    @JsonProperty("packagingOutcome")
    private FhirPackagingOutcome packagingOutcome;
    
    public FhirPackage() {
        
    }
    
    public FhirPackage(String measurePackage, String libraryPackage, FhirPackagingOutcome packagingOutcome) {
        this.measurePackage = measurePackage;
        this.libraryPackage = libraryPackage;
        this.packagingOutcome = packagingOutcome;
    }

    /**
     * @return the measurePackage
     */
    @JsonProperty("measurePackage") 
    public String getMeasurePackage() {
        return measurePackage;
    }

    /**
     * @param measurePackage the measurePackage to set
     */
    @JsonProperty("measurePackage")
    public void setMeasurePackage(String measurePackage) {
        this.measurePackage = measurePackage;
    }

    /**
     * @return the libraryPackage
     */
    @JsonProperty("libraryPackage")
    public String getLibraryPackage() {
        return libraryPackage;
    }

    /**
     * @param libraryPackage the libraryPackage to set
     */
    @JsonProperty("libraryPackage")
    public void setLibraryPackage(String libraryPackage) {
        this.libraryPackage = libraryPackage;
    }

    /**
     * @return the packagingOutcome
     */
    @JsonProperty("packagingOutcome")
    public FhirPackagingOutcome getPackagingOutcome() {
        return packagingOutcome;
    }

    /**
     * @param packagingOutcome the packagingOutcome to set
     */
    @JsonProperty("packagingOutcome")
    public void setPackagingOutcome(FhirPackagingOutcome packagingOutcome) {
        this.packagingOutcome = packagingOutcome;
    }
}
