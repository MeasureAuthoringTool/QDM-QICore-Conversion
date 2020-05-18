package gov.cms.mat.fhir.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hl7.fhir.r4.model.Library;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"name", "version"})
public class FhirIncludeLibraryReferences {
    String name;
    String version;
    String referenceEndpoint;
    boolean searchResult;

    Library library;
    boolean scannedForIncludedLibraries = false;
}
