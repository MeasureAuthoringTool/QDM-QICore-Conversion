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

    //"codesystem \"SNOMEDCT:2017-09\": 'http://snomed.info/sct/731000124108' version 'urn:hl7:version:2016-09'";

    private static final String CODE_SYSTEM_TEMPLATE = "codesystem \"%s\": '%s'";
    private static final String VERSION_TEMPLATE = " version '%s'";

    String name;
    @Setter
    String urnOid;
    String line;
    String version;


    @Override
    public void setToFhir() {
        log.trace("Currently a no op");
    }

    @Override
    public String createCql() {
        String converted = String.format(CODE_SYSTEM_TEMPLATE, name, urnOid);

        if (StringUtils.isEmpty(version)) {
            return converted;
        } else {
            return converted + String.format(VERSION_TEMPLATE, version);
        }
    }
}
