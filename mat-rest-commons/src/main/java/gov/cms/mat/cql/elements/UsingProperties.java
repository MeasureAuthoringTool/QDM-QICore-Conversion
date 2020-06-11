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
public class UsingProperties extends BaseProperties {
    private static final String TEMPLATE = "using %s version '%s'";  // using QDM version '5.4'
    String libraryType;
    String version;
    String line;
    String comment;

    @Override
    public void setToFhir() {
        //using FHIR version '4.0.0'
        this.libraryType = LIBRARY_FHIR_TYPE;
        this.version = LIBRARY_FHIR_VERSION;
    }

    public boolean isFhir() {
        return StringUtils.isNotEmpty(libraryType) && (libraryType.equals(BaseProperties.LIBRARY_FHIR_TYPE) || libraryType.equals("4.0.0"));
    }

    @Override
    public String createCql() {
        String cql = String.format(TEMPLATE, libraryType, version);

        if (StringUtils.isEmpty(comment)) {
            return cql;
        } else {
            return cql + comment;
        }
    }
}