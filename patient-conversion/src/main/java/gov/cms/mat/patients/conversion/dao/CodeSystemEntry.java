package gov.cms.mat.patients.conversion.dao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodeSystemEntry {
    String oid;
    String url;
    String name;
}
