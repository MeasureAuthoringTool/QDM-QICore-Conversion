package gov.cms.mat.fhir.rest.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class ConversionMapping {

    int hashValue;

    String title;

    String matDataTypeDescription;

    String matAttributeName;

    String fhirR4QiCoreMapping;

    String fhirResource;

    String fhirElement;

    String fhirType;

    List<String> dropDownValues;

    List<String> recommendationValues;

    String helpWording;

    public ConversionMapping(int hashValue,
                             String title,
                             String matDataTypeDescription,
                             String matAttributeName,
                             String fhirR4QiCoreMapping,
                             String fhirResource,
                             String fhirElement,
                             String fhirType,
                             List<String> dropDownValues,
                             List<String> recommendationValues,
                             String helpWording) {
        this.hashValue = hashValue;
        this.title = title;
        this.matDataTypeDescription = matDataTypeDescription;
        this.matAttributeName = matAttributeName;
        this.fhirElement = fhirElement;
        this.fhirResource = fhirResource;
        this.fhirType = fhirType;
        this.fhirR4QiCoreMapping = fhirR4QiCoreMapping;
        this.dropDownValues = dropDownValues;
        this.recommendationValues = recommendationValues;
        this.helpWording = helpWording;
    }

    public ConversionMapping() {

    }

}
