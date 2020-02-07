package gov.cms.mat.cql.parsers;

import gov.cms.mat.cql.CqlParser;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public interface UsingParser {
    String[] getLines();

    default CqlParser.UsingProperties getUsing() {
        return Arrays.stream(getLines())
                .filter(l -> l.startsWith("using"))
                .map(this::buildUsingProperties)
                .findFirst()
                .orElse(null);
    }

    default CqlParser.UsingProperties buildUsingProperties(String line) {
        return CqlParser.UsingProperties.builder()
                .libraryType(getUsingLibraryType(line))
                .version(getUsingLibraryVersion(line))
                .build();
    }

    default String getUsingLibraryType(String line) {
        return StringUtils.substringBetween(line, "using ", " version ");
    }

    default String getUsingLibraryVersion(String line) {
        return StringUtils.substringBetween(line, " version '", "'");
    }
}
