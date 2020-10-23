package gov.cms.mat.patients.conversion.dao;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CodeSystemEntry {
    String oid;
    String url;
    String name;
}
