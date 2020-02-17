package gov.cms.mat.cql.elements;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class IncludeProperties extends BaseProperties {
    private static final String TEMPLATE = "include %s version '%s' %s";  // include myFunctions version '4.1.000' called Global

    String name;
    String version;
    String using; //can be empty string ""
    String line;

    @Override
    public void setToFhir() {
        name = name + LIBRARY_FHIR_EXTENSION;
    }

    @Override
    public String createCql() {
        return String.format(TEMPLATE, name, version, using);
    }
}
