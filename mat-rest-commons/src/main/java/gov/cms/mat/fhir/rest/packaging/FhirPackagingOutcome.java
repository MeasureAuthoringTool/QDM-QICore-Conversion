/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.rest.packaging;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 *
 * @author duanedecouteau
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"name","version","result", "packagingType","packagingMessage"})
public class FhirPackagingOutcome {
    @JsonProperty("name")
    private String name;
    @JsonProperty("version")
    private String version;
    @JsonProperty("result")
    private boolean result = false;
    @JsonProperty("packagingType")
    private String packagingType = "Unknown";
    @JsonProperty("packagingMessage")
    private String packagingMessage = "No Information Available";
    @JsonProperty("bundleFormat")
    private String bundleFormat;
    
    public FhirPackagingOutcome(boolean result, String packagingType, String packagingMessage) {
        this.result = result;
        this.packagingType = packagingType;
        this.packagingMessage = packagingMessage;
    }
    
    public FhirPackagingOutcome() {
        
    }

    /**
     * @return the result
     */
    @JsonProperty("result")
    public boolean isResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    @JsonProperty("result")
    public void setResult(boolean result) {
        this.result = result;
    }

    /**
     * @return the packagingType
     */
    @JsonProperty("packagingType")
    public String getPackagingType() {
        return packagingType;
    }

    /**
     * @param packagingType the packagingType to set
     */
    @JsonProperty("packagingType")
    public void setPackagingType(String packagingType) {
        this.packagingType = packagingType;
    }

    /**
     * @return the packagingMessage
     */
    @JsonProperty("packagingMessage")
    public String getPackagingMessage() {
        return packagingMessage;
    }

    /**
     * @param packagingMessage the packagingMessage to set
     */
    @JsonProperty("packagingMessage")
    public void setPackagingMessage(String packagingMessage) {
        this.packagingMessage = packagingMessage;
    }

    /**
     * @return the name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the bundleFormat
     */
    @JsonProperty("bundleFormat")
    public String getBundleFormat() {
        return bundleFormat;
    }

    /**
     * @param bundleFormat the bundleFormat to set
     */
    @JsonProperty("bundleFormat")
    public void setBundleFormat(String bundleFormat) {
        this.bundleFormat = bundleFormat;
    }

    /**
     * @return the version
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }
    
}
