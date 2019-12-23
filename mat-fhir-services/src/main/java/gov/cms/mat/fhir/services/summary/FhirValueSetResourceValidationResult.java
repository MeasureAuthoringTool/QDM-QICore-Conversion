package gov.cms.mat.fhir.services.summary;

import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.rest.dto.ValueSetResult;
import gov.cms.mat.fhir.services.components.xml.XmlSource;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class FhirValueSetResourceValidationResult {
    List<FhirResourceValidationResult> fhirResourceValidationResults = new ArrayList<>();
    private List<ValueSetResult> valueSetResults = new ArrayList<>();
    private ConversionType valueSetConversionType;
    private XmlSource xmlSource;
    //private String measureId;

//    @XmlElement
//    public String getMeasureId() {
//        return measureId;
//    }
//
//    public void setMeasureId(String measureId) {
//        this.measureId = measureId;
//    }

    @XmlElement
    public XmlSource getXmlSource() {
        return xmlSource;
    }

    public void setXmlSource(XmlSource xmlSource) {
        this.xmlSource = xmlSource;
    }

    @XmlElement
    public List<FhirResourceValidationResult> getFhirResourceValidationResults() {
        return fhirResourceValidationResults;
    }

    public void setFhirResourceValidationResults(List<FhirResourceValidationResult> fhirResourceValidationResults) {
        this.fhirResourceValidationResults = fhirResourceValidationResults;
    }

    @XmlElement
    public List<ValueSetResult> getValueSetResults() {
        return valueSetResults;
    }

    public void setValueSetResults(List<ValueSetResult> valueSetResults) {
        this.valueSetResults = valueSetResults;
    }

    @XmlElement
    public ConversionType getValueSetConversionType() {
        return valueSetConversionType;
    }

    public void setValueSetConversionType(ConversionType valueSetConversionType) {
        this.valueSetConversionType = valueSetConversionType;
    }
}
