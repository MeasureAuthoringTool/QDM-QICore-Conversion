package gov.cms.mat.fhir.services.summary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class MeasureVersionExportId {
    String id;
    String version;
}
