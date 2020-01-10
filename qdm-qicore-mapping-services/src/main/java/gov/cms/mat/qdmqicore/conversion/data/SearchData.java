package gov.cms.mat.qdmqicore.conversion.data;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class SearchData {
    private String matDataTypeDescription;
    private String fhirResource;
    private String matAttributeName;
    private String fhirR4QiCoreMapping;
    private String fhirElement;
    private String fhirType;
}
