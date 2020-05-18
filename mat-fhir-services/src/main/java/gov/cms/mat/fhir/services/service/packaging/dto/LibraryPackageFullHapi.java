package gov.cms.mat.fhir.services.service.packaging.dto;

import lombok.Builder;
import lombok.Getter;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;

@Builder
@Getter
public class LibraryPackageFullHapi {
    Library library;
    Bundle includeBundle;
}
