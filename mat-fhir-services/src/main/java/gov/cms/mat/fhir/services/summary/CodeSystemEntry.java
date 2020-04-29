package gov.cms.mat.fhir.services.summary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CodeSystemEntry {
    private String oid;
    private String url;
    private String name;
    private String defaultVsacVersion;
}
