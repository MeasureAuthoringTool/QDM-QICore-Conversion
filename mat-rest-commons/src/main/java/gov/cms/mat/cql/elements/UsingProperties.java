package gov.cms.mat.cql.elements;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class UsingProperties extends BaseProperties {
    private static final String TEMPLATE = "using %s version '%s'";  // using QDM version '5.4'
    String libraryType;
    String version;
    String line;

    @Override
    public void setToFhir() {
        //using FHIR version '4.0.0'
        this.libraryType = LIBRARY_FHIR_TYPE;
        this.version = LIBRARY_FHIR_VERSION;
    }

    @Override
    public String createCql() {
        return String.format(TEMPLATE, libraryType, version);
    }
}