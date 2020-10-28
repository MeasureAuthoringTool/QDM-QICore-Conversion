package gov.cms.mat.cql.parsers;

import org.apache.commons.lang3.StringUtils;

public interface OidParser {

    default String findName(String line) {
        return StringUtils.substringBetween(line, "\"", "\"");
    }

    default String findOid(String line) {
        return StringUtils.substringBetween(line, ": '", "'");
    }
}
