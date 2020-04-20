package gov.cms.mat.cql.elements;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class LibraryProperties extends BaseProperties {
    private static final String TEMPLATE = "library %s version '%s' ";  // library myCQL version '1.0.000'
    String name;
    String version;
    String line;

    @Override
    public void setToFhir() {
       // name = name + LIBRARY_FHIR_EXTENSION;
    }

    @Override
    public String createCql() {
        return String.format(TEMPLATE, name, version);
    }
}
