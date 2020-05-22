package gov.cms.mat.fhir.services.service.packaging.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MeasurePackageFullData {
    String includeBundle;
    String measure;
    String library;
}
