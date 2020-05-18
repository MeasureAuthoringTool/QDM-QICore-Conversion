package gov.cms.mat.fhir.services.service.packaging.dto;

import lombok.Builder;
import lombok.Getter;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;

@Builder
@Getter
public class MeasurePackageMinimumHapi {
    Measure measure;
    Library library;
}
