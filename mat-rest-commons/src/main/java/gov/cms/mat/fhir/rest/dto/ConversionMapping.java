package gov.cms.mat.fhir.rest.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ConversionMapping {
    String title;

    String matDataTypeDescription;

    String matAttributeName;

    String fhirR4QiCoreMapping;

    String fhirResource;

    String fhirElement;

    String fhirType;
    
    public ConversionMapping(String title, String matDataTypeDescription, String matAttributeName, String fhirR4QiCoreMapping, String fhirResource, String fhirElement, String fhirType) {
        this.title = title;
        this.matDataTypeDescription = matDataTypeDescription;
        this.matAttributeName = matAttributeName;
        this.fhirElement = fhirElement;
        this.fhirResource = fhirResource;
        this.fhirType = fhirType;
        this.fhirR4QiCoreMapping = fhirR4QiCoreMapping;
    }
   
    public ConversionMapping() {
        
    }
    
}
