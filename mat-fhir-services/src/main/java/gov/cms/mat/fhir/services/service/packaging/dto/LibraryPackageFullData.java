package gov.cms.mat.fhir.services.service.packaging.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LibraryPackageFullData {
    String library;
    String includeBundle;
}
