package gov.cms.mat.cql.parsers;

import gov.cms.mat.cql.CqlParser;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface IncludeParser {
    String[] getLines();

    default List<CqlParser.IncludeProperties> getIncludes() {
        return Arrays.stream(getLines())
                .filter(l -> l.startsWith("include"))
                .map(this::buildIncludeProperties)
                .collect(Collectors.toList());
    }

    default CqlParser.IncludeProperties buildIncludeProperties(String line) {
        return CqlParser.IncludeProperties.builder()
                .name(getIncludeName(line))
                .version(getVersion(line))
                .build();
    }

    default String getIncludeName(String line) {
        return StringUtils.substringBetween(line, "include ", " version ");
    }

    default String getVersion(String line) {
        return StringUtils.substringBetween(line, " version '", "' ");
    }
}
