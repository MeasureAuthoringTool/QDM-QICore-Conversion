package gov.cms.mat.qdmqicore.conversion.data;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SearchData {
    private String matDataTypeDescription;
    private String fhirResource;
    private String matAttributeName;
    private String fhirR4QiCoreMapping;
    private String fhirElement;
    private String fhirType;
}
