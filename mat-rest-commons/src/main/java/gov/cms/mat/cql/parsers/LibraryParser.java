package gov.cms.mat.cql.parsers;

import gov.cms.mat.cql.elements.LibraryProperties;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public interface LibraryParser {
    String[] getLines();

    default LibraryProperties getLibrary() {
        return Arrays.stream(getLines())
                .filter(l -> l.startsWith("library"))
                .map(this::buildLibraryProperties)
                .findFirst()
                .orElseThrow(() -> new ParseException("Cannot find library"));
    }

    default LibraryProperties buildLibraryProperties(String line) {
        return LibraryProperties.builder()
                .name(getLibraryName(line))
                .version(getLibraryVersion(line))
                .line(line)
                .build();
    }

    default String getLibraryName(String line) {
        return StringUtils.substringBetween(line, "library ", " version ");
    }

    default String getLibraryVersion(String line) {
        return StringUtils.substringBetween(line, " version '", "'");
    }
}
