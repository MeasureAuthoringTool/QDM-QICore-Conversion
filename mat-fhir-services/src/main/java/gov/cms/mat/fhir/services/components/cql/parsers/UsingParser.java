package gov.cms.mat.fhir.services.components.cql.parsers;

import gov.cms.mat.fhir.services.components.cql.CqlParser;
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
                .libraryType(getLibraryType(line))
                .version(getLibraryVersion(line))
                .build();
    }

    default String getLibraryType(String line) {
        return StringUtils.substringBetween(line, "using ", " version ");
    }

    default String getLibraryVersion(String line) {
        return StringUtils.substringBetween(line, " version '", "'");
    }
}
