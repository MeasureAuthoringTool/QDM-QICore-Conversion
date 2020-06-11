package gov.cms.mat.cql.elements;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

@Builder
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class LibraryProperties extends BaseProperties {
    private static final String TEMPLATE = "library %s version '%s'";  // library myCQL version '1.0.000'
    String name;
    String version;
    String line;

    String comment;

    @Override
    public void setToFhir() {
      // no-op
    }

    @Override
    public String createCql() {
        String cql = String.format(TEMPLATE, name, version);

        if (StringUtils.isEmpty(comment)) {
            return cql;
        } else {
            return cql + comment;
        }
    }
}
