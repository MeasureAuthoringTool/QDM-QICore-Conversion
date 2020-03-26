package gov.cms.mat.fhir.rest.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 *
 * @author duanedecouteau
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FhirIncludeLibraryResult {
    
    String libraryName;
    String libraryVersion;
    boolean outcome;
    List<FhirIncludeLibraryReferences> libraryReferences;
}
