package gov.cms.mat.cql.elements;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Builder
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class CodeSystemProperties extends BaseProperties {
    private static final String CODE_SYSTEM_TEMPLATE = "codesystem \"%s\": '%s'";
    private static final String VERSION_TEMPLATE = " version '%s'";

    String name;
    @Setter
    String urnOid;
    String line;
    @Setter
    String version;

    @Override
    public void setToFhir() {
        log.trace("Currently a no op");
    }

    @Override
    public String createCql() {
        String converted = String.format(CODE_SYSTEM_TEMPLATE, name, urnOid);
        return StringUtils.isEmpty(version) ? converted : converted + String.format(VERSION_TEMPLATE, version);
    }
}
