package gov.cms.mat.cql.elements;

import lombok.*;
import org.apache.commons.lang3.StringUtils;

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

    @Setter
    String called; // is the symbolic optional name

    @Setter
    boolean display;

    @Override
    public void setToFhir() {
        name = name + LIBRARY_FHIR_EXTENSION;
    }

    @Override
    public String createCql() {
        String cql = String.format(TEMPLATE, name, version, using).trim();

        if (StringUtils.isEmpty(called)) {
            return cql;
        } else {
            return cql + " called " + called;
        }
    }
}
