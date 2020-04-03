package gov.cms.mat.fhir.services.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FhirIncludeLibraryReferences {

    String name;
    String version;
    String referenceEndpoint;
    boolean searchResult;
}

