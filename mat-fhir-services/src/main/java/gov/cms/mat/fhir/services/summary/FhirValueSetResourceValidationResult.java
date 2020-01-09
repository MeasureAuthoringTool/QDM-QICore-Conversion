package gov.cms.mat.fhir.services.summary;

import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.rest.dto.ValueSetConversionResults;
import gov.cms.mat.fhir.services.components.xml.XmlSource;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class FhirValueSetResourceValidationResult {
    List<ValueSetConversionResults> valueSetConversionResults = new ArrayList<>();

    private ConversionType valueSetConversionType;
    private XmlSource xmlSource;

    @XmlElement
    public XmlSource getXmlSource() {
        return xmlSource;
    }

    public void setXmlSource(XmlSource xmlSource) {
        this.xmlSource = xmlSource;
    }


    @XmlElement
    public List<ValueSetConversionResults> getValueSetConversionResults() {
        return valueSetConversionResults;
    }

    public void setValueSetConversionResults(List<ValueSetConversionResults> valueSetConversionResults) {
        this.valueSetConversionResults = valueSetConversionResults;
    }

    @XmlElement
    public ConversionType getValueSetConversionType() {
        return valueSetConversionType;
    }

    public void setValueSetConversionType(ConversionType valueSetConversionType) {
        this.valueSetConversionType = valueSetConversionType;
    }
}
